package org.dan.ping.pong.mock.simulator;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.dan.ping.pong.app.match.MatchResource.COMPLETE_MATCH;
import static org.dan.ping.pong.app.tournament.TournamentState.Close;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.app.match.FinalMatchScore;
import org.dan.ping.pong.app.match.ForTestBidDao;
import org.dan.ping.pong.app.match.ForTestMatchDao;
import org.dan.ping.pong.app.match.IdentifiedScore;
import org.dan.ping.pong.app.match.MatchDao;
import org.dan.ping.pong.app.match.MatchType;
import org.dan.ping.pong.app.match.OpenMatchForJudge;
import org.dan.ping.pong.app.table.TableDao;
import org.dan.ping.pong.app.table.TableInfo;
import org.dan.ping.pong.app.tournament.TournamentDao;
import org.dan.ping.pong.app.tournament.TournamentInfo;
import org.dan.ping.pong.app.tournament.TournamentState;
import org.dan.ping.pong.app.user.UserLink;
import org.dan.ping.pong.mock.DaoEntityGeneratorWithAdmin;
import org.dan.ping.pong.mock.MyLocalRest;
import org.dan.ping.pong.mock.RestEntityGeneratorWithAdmin;
import org.dan.ping.pong.mock.TestAdmin;
import org.dan.ping.pong.mock.TestUserSession;
import org.dan.ping.pong.mock.TournamentProps;
import org.dan.ping.pong.mock.UserSessionGenerator;
import org.hamcrest.Matchers;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

@Slf4j
public class Simulator {
    @Inject
    private TestAdmin testAdmin;

    @Inject
    private RestEntityGeneratorWithAdmin restGenerator;

    @Inject
    private DaoEntityGeneratorWithAdmin daoGenerator;

    @Inject
    private UserSessionGenerator userSessionGenerator;

    @Inject
    private MatchDao matchDao;

    @Inject
    private MyLocalRest rest;

    @Inject
    private ForTestMatchDao testMatchDao;

    @Inject
    private TableDao tableDao;

    @Inject
    private ForTestBidDao forTestBidDao;

    @Inject
    private TournamentDao tournamentDao;

    public void simulate(SimulatorParams params, TournamentScenario scenario) {
        scenario.setParams(params);
        setupEnvironment(params, scenario);
        restGenerator.beginTournament(scenario.getTid());
        try {
            final boolean allMatchesComplete = expendAllMatches(scenario);
            if (allMatchesComplete) {
                validateCompleteTournament(scenario);
            } else {
                throw new IllegalArgumentException("not implemented");
            }
        } catch (IllegalStateException|AssertionError e) {
            log.info("Scenario {} failed", scenario);
            throw e;
        }
    }

    private void validateCompleteTournament(TournamentScenario scenario) {
        assertEquals(emptyList(),
                testMatchDao.findIncompleteTournamentMatches(scenario.getTid()));
        List<TableInfo> tables = tableDao.findFreeTables(scenario.getTid());
        assertThat(tables, Matchers.hasSize(scenario.getParams().getTables()));
        assertEquals(emptyList(),
                tables.stream().map(TableInfo::getMid)
                        .filter(Optional::isPresent)
                        .collect(toList()));
        List<BidState> bidStates = asList(BidState.Win1, BidState.Win2, BidState.Win3);
        for (int i = 0; i < scenario.getChampions().size(); ++i) {
            assertEquals(singletonList(scenario.getChampions().get(i)),
                    forTestBidDao.findByTidAndState(scenario.getTid(), bidStates.get(i))
                            .stream().map(uid -> scenario.getUidPlayer().get(uid))
                            .collect(toList()));
        }
        assertEquals(
                scenario.getPlayersSessions().keySet().stream()
                        .filter(player -> !scenario.getChampions().contains(player))
                        .collect(toSet()),
                forTestBidDao.findByTidAndState(scenario.getTid(), BidState.Lost)
                        .stream().map(uid -> scenario.getUidPlayer().get(uid))
                        .collect(toSet()));
        assertEquals(Optional.of(Close),
                tournamentDao.getById(scenario.getTid())
                        .map(TournamentInfo::getState));
    }

    private boolean expendAllMatches(TournamentScenario scenario) {
        final int[] completedMatches = new int[1];
        while (true) {
            completedMatches[0] = 0;
            final List<OpenMatchForJudge> openMatches = matchDao.findOpenMatchesFurJudge(daoGenerator.getAdminUid());
            assertThat(tableDao.findFreeTables(scenario.getTid()),
                    Matchers.hasSize(scenario.getParams().getTables() - openMatches.size()));
            completeOpenMatches(scenario, completedMatches, openMatches);

            if (openMatches.isEmpty()) {
                return true;
            } else if (completedMatches[0] == 0) {
                if (scenario.getPlayOffMatches().isEmpty() && scenario.getGroupMatches().isEmpty()) {
                    return false;
                }
                throw new IllegalStateException("Scenario stuck at matches "
                        + openMatches.stream().map(OpenMatchForJudge::getMid).collect(toList())
                        + " player pairs "
                        + openMatches.stream()
                        .map(match -> matchToPlayers(scenario, match))
                        .collect(toList()));
            }
        }
    }

    private void completeOpenMatches(TournamentScenario scenario,
            int[] completedMatches, List<OpenMatchForJudge> openMatches) {
        for (OpenMatchForJudge openMatch : openMatches) {
            findGame(scenario, openMatch).ifPresent(game -> {
                ++completedMatches[0];
                rest.voidPost(COMPLETE_MATCH, testAdmin,
                        FinalMatchScore.builder()
                                .mid(openMatch.getMid())
                                .scores(asList(
                                        IdentifiedScore.builder()
                                                .score(game.getOutcome().first())
                                                .uid(scenario.getPlayersSessions()
                                                        .get(game.getParticipants()
                                                                .get(0)).getUid())
                                                .build(),
                                        IdentifiedScore.builder()
                                                .score(game.getOutcome().second())
                                                .uid(scenario.getPlayersSessions()
                                                        .get(game.getParticipants()
                                                                .get(1)).getUid())
                                                .build()))
                                .build());
            });
        }
    }

    private Optional<GameEnd> findGame(TournamentScenario scenario, OpenMatchForJudge openMatch) {
        final Set<Player> players = matchToPlayers(scenario, openMatch);
        final Map<Set<Player>, GameEnd> matchMap = openMatch.getType() == MatchType.Group
                ? scenario.getGroupMatches()
                : scenario.getPlayOffMatches();
        if (matchMap.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(ofNullable(matchMap.remove(players))
                .orElseThrow(() -> new IllegalStateException("Match between "
                        + players + " is not expected at "
                        + openMatch.getType() + " stage")));
    }

    private Set<Player> matchToPlayers(TournamentScenario scenario, OpenMatchForJudge match) {
        return match.getParticipants().stream().map(UserLink::getUid)
                .map(uid -> ofNullable(scenario.getUidPlayer().get(uid))
                        .orElseThrow(() -> new IllegalStateException("uid "
                                + uid + " is not known among "
                                + scenario.getUidPlayer().keySet())))
                .collect(toSet());
    }

    private void setupEnvironment(SimulatorParams params, TournamentScenario scenario) {
        final int placeId = daoGenerator.genPlace(params.getTables());
        scenario.setPlaceId(placeId);
        final int tid = daoGenerator.genTournament(placeId, TournamentProps.builder()
                .maxGroupSize(params.getMaxGroupSize())
                .quitsFromGroup(params.getQuitsFromGroup())
                .state(TournamentState.Draft)
                .build());
        scenario.setTid(tid);
        final Map<Player, TestUserSession> playersSession = scenario.getPlayersSessions();
        final Iterator<TestUserSession> sessions = userSessionGenerator
                .generateUserSessions(playersSession.size())
                .iterator();
        for (Player player : playersSession.keySet()) {
            TestUserSession user = sessions.next();
            playersSession.put(player, user);
            scenario.getUidPlayer().put(user.getUid(), player);
        }
        for (PlayerCategory category : scenario.getCategoryDbId().keySet()) {
            final int catId = daoGenerator.genCategory(tid);
            scenario.getCategoryDbId().put(category, catId);
            restGenerator.enlistParticipants(tid,
                    catId, scenario.getPlayersByCategories().get(category)
                            .stream().map(player -> scenario.getPlayersSessions().get(player))
                            .collect(toList()));
        }
    }
}
