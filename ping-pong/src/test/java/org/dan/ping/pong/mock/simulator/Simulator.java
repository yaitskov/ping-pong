package org.dan.ping.pong.mock.simulator;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.dan.ping.pong.app.auth.AuthResource.DEV_CLEAN_SIGN_IN_TOKEN_TABLE;
import static org.dan.ping.pong.app.match.MatchResource.COMPLETE_MATCH;
import static org.dan.ping.pong.app.match.MatchType.Group;
import static org.dan.ping.pong.app.tournament.TournamentState.Close;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.app.match.FinalMatchScore;
import org.dan.ping.pong.app.match.ForTestBidDao;
import org.dan.ping.pong.app.match.ForTestMatchDao;
import org.dan.ping.pong.app.match.IdentifiedScore;
import org.dan.ping.pong.app.match.MatchDao;
import org.dan.ping.pong.app.match.OpenMatchForJudge;
import org.dan.ping.pong.app.table.TableDao;
import org.dan.ping.pong.app.table.TableInfo;
import org.dan.ping.pong.app.tournament.TournamentDao;
import org.dan.ping.pong.app.tournament.TournamentInfo;
import org.dan.ping.pong.app.tournament.TournamentState;
import org.dan.ping.pong.app.user.UserLink;
import org.dan.ping.pong.app.user.UserType;
import org.dan.ping.pong.mock.DaoEntityGenerator;
import org.dan.ping.pong.mock.MyRest;
import org.dan.ping.pong.mock.RestEntityGenerator;
import org.dan.ping.pong.mock.TestUserSession;
import org.dan.ping.pong.mock.TournamentProps;
import org.dan.ping.pong.mock.UserSessionGenerator;
import org.dan.ping.pong.mock.ValueGenerator;
import org.hamcrest.Matchers;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

@Slf4j
public class Simulator {
    private TestUserSession testAdmin;

    @Inject
    private RestEntityGenerator restGenerator;

    @Inject
    private DaoEntityGenerator daoGenerator;

    @Inject
    private UserSessionGenerator userSessionGenerator;

    @Inject
    private MatchDao matchDao;

    @Inject
    private MyRest rest;

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
        if (!scenario.isBegin()) {
            return;
        }
        restGenerator.beginTournament(testAdmin, scenario.getTid());
        try {
            final boolean allMatchesComplete = expendAllMatches(scenario);
            if (allMatchesComplete && !scenario.isIgnoreUnexpectedGames()
                    && !scenario.getAutoResolution().isPresent()) {
                validateCompleteTournament(scenario);
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

    private void showUnHeldMatches(String label, Map<Set<Player>, GameEnd> matches) {
        if (!matches.isEmpty()) {
            log.error("Unheld {} matches: {}", label,
                    Joiner.on(", ").join(matches.keySet()));
        }
    }

    private boolean expendAllMatches(TournamentScenario scenario) {
        final int[] completedMatches = new int[1];
        while (true) {
            completedMatches[0] = 0;
            final List<OpenMatchForJudge> openMatches = matchDao.findOpenMatchesFurJudge(
                    testAdmin.getUid());
            assertThat(tableDao.findFreeTables(scenario.getTid()),
                    Matchers.hasSize(scenario.getParams().getTables() - openMatches.size()));
            completeOpenMatches(scenario, completedMatches, openMatches);
            if (scenario.isIgnoreUnexpectedGames()
                    && scenario.getGroupMatches().isEmpty()
                    && scenario.getPlayOffMatches().isEmpty()) {
                return openMatches.isEmpty();
            }
            if (openMatches.isEmpty()) {
                showUnHeldMatches("groups", scenario.getGroupMatches());
                showUnHeldMatches("playOff", scenario.getPlayOffMatches());
                if (!scenario.getGroupMatches().isEmpty()
                        || !scenario.getPlayOffMatches().isEmpty()) {
                    throw new IllegalStateException("Some of matches are not held");
                }
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
            final Set<Player> players = matchToPlayers(scenario, openMatch);
            findGame(scenario, openMatch, players).ifPresent(game -> {
                final Pause pause = scenario.getPauseOnMatches().getOrDefault(players, Pause.NonStop);
                pause.pauseBefore(players);
                ++completedMatches[0];
                log.info("Match id {} outcome {}", openMatch.getMid(), game);
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
                pause.pauseAfter(players);
            });
        }
    }

    private Optional<GameEnd> findGame(TournamentScenario scenario,
            OpenMatchForJudge openMatch,
            Set<Player> players) {
        final Map<Set<Player>, GameEnd> matchMap = openMatch.getType() == Group
                ? scenario.getGroupMatches()
                : scenario.getPlayOffMatches();
        final GameEnd gameEnd = matchMap.remove(players);
        if (gameEnd == null) {
            if (scenario.getAutoResolution().isPresent()) {
                Iterator<Player> iterator = players.iterator();
                return Optional.of(GameEnd.game(iterator.next(),
                        scenario.getAutoResolution().get().choose(players),
                        iterator.next()));
            }
            if (scenario.isIgnoreUnexpectedGames()) {
                return empty();
            } else {
                throw new IllegalStateException("Match between "
                        + players + " is not expected at "
                        + openMatch.getType() + " stage");
            }
        }
        return Optional.of(gameEnd);
    }

    private Set<Player> matchToPlayers(TournamentScenario scenario, OpenMatchForJudge match) {
        return match.getParticipants().stream().map(UserLink::getUid)
                .map(uid -> ofNullable(scenario.getUidPlayer().get(uid))
                        .orElseThrow(() -> new IllegalStateException("uid "
                                + uid + " is not known among "
                                + scenario.getUidPlayer().keySet())))
                .collect(toSet());
    }

    @Inject
    private ValueGenerator valueGenerator;

    private void setupEnvironment(SimulatorParams params, TournamentScenario scenario) {
        rest.voidAnonymousPost(DEV_CLEAN_SIGN_IN_TOKEN_TABLE, "");
        final String prefix = scenario.getName()
                .orElseGet(() -> "todo")
                + " " + valueGenerator.genName(8);
        testAdmin = userSessionGenerator.generate(prefix + " admin", UserType.Admin);
        restGenerator.generateSignInLinks(singletonList(testAdmin));
        final int placeId = daoGenerator.genPlace(prefix, testAdmin.getUid(), params.getTables());
        scenario.setPlaceId(placeId);
        final int tid = daoGenerator.genTournament(prefix, testAdmin.getUid(),
                placeId, TournamentProps.builder()
                .maxGroupSize(params.getMaxGroupSize())
                .quitsFromGroup(params.getQuitsFromGroup())
                .state(TournamentState.Draft)
                .build());
        scenario.setTid(tid);
        final Collection<Player> players = scenario.getPlayersByCategories().values();
        final Map<Player, TestUserSession> playersSession = scenario.getPlayersSessions();
        final List<String> playerLabels = players.stream()
                .map(p -> "_p" + p.getNumber())
                .collect(toList());
        final List<TestUserSession> userSessions = userSessionGenerator
                .generateUserSessions(prefix, playerLabels);
        final Iterator<TestUserSession> sessions = userSessions.iterator();
        assertEquals(players.size(), userSessions.size());
        restGenerator.generateSignInLinks(userSessions);
        for (Player player : players) {
            TestUserSession user = sessions.next();
            playersSession.put(player, user);
            scenario.getUidPlayer().put(user.getUid(), player);
        }
        for (PlayerCategory category : scenario.getCategoryDbId().keySet()) {
            final int catId = daoGenerator.genCategory(prefix, tid);
            scenario.getCategoryDbId().put(category, catId);
            Map<TestUserSession, EnlistMode> m = scenario.getPlayerPresence().keySet()
                    .stream().collect(Collectors.toMap(
                            player -> scenario.getPlayersSessions().get(player),
                            player -> scenario.getPlayerPresence().get(player)));
            restGenerator.enlistParticipants(rest, testAdmin,
                    m, tid,
                    catId, scenario.getPlayersByCategories().get(category)
                            .stream().map(player -> scenario.getPlayersSessions().get(player))
                            .collect(toList()));
        }
    }
}
