package org.dan.ping.pong.mock.simulator;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.dan.ping.pong.app.auth.AuthResource.DEV_CLEAN_SIGN_IN_TOKEN_TABLE;
import static org.dan.ping.pong.app.bid.BidState.Expl;
import static org.dan.ping.pong.app.bid.BidState.Lost;
import static org.dan.ping.pong.app.bid.BidState.Quit;
import static org.dan.ping.pong.app.match.MatchResource.SCORE_SET;
import static org.dan.ping.pong.app.tournament.TournamentResource.TOURNAMENT_RESIGN;
import static org.dan.ping.pong.app.tournament.TournamentState.Close;
import static org.dan.ping.pong.mock.simulator.Hook.AfterMatch;
import static org.dan.ping.pong.mock.simulator.Hook.AfterScore;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.app.match.FinalMatchScore;
import org.dan.ping.pong.app.match.ForTestBidDao;
import org.dan.ping.pong.app.match.ForTestMatchDao;
import org.dan.ping.pong.app.match.IdentifiedScore;
import org.dan.ping.pong.app.match.MatchDao;
import org.dan.ping.pong.app.match.OpenMatchForJudge;
import org.dan.ping.pong.app.match.SetScoreResult;
import org.dan.ping.pong.app.table.TableDao;
import org.dan.ping.pong.app.table.TableInfo;
import org.dan.ping.pong.app.tournament.SetScoreResultName;
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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

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

    public void simulate(TournamentScenario scenario) {
        try {
            setupEnvironment(scenario);
            if (!scenario.isBegin()) {
                return;
            }
            restGenerator.beginTournament(scenario.getTestAdmin(), scenario.getTid());
            final boolean allMatchesComplete = expendAllMatches(scenario);
            if (allMatchesComplete && !scenario.isIgnoreUnexpectedGames()
                    && !scenario.getAutoResolution().isPresent()) {
                validateCompleteTournament(scenario);
            }
        } catch (IllegalStateException|AssertionError e) {
            log.info("Scenario {} failed", scenario, e);
            throw e;
        }
    }

    private void validateCompleteTournament(TournamentScenario scenario) {
        assertEquals(emptyList(),
                testMatchDao.findIncompleteTournamentMatches(scenario.getTid()));
        List<TableInfo> tables = tableDao.findFreeTables(scenario.getTid());
        assertThat(tables, Matchers.hasSize(scenario.getTables()));
        assertEquals(emptyList(),
                tables.stream().map(TableInfo::getMid)
                        .filter(Optional::isPresent)
                        .collect(toList()));
        List<BidState> bidStates = asList(BidState.Win1, BidState.Win2, BidState.Win3);
        for (PlayerCategory playerCategory : scenario.getChampions().keySet()) {
            final int cid = scenario.getCategoryDbId().get(playerCategory);
            assertEquals(scenario.getChampions().get(playerCategory),
                    forTestBidDao.findByTidAndState(scenario.getTid(),
                            cid,
                            bidStates)
                            .stream().map(uid -> scenario.getUidPlayer().get(uid))
                            .collect(toList()));
            assertEquals(
                    scenario.getPlayersSessions().keySet().stream()
                            .filter(player -> !scenario.getChampions().get(playerCategory).contains(player))
                            .filter(player -> scenario.getPlayersCategory().get(player).equals(playerCategory))
                            .collect(toSet()),
                    forTestBidDao.findByTidAndState(scenario.getTid(), cid, asList(Lost, Quit, Expl))
                            .stream().map(uid -> scenario.getUidPlayer().get(uid))
                            .collect(toSet()));
        }
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
                    Matchers.hasSize(scenario.getTables() - openMatches.size()));
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
                    throw new IllegalStateException("Some of matches are not held: left in group "
                            + scenario.getGroupMatches().keySet()
                            + " and in play off " + scenario.getPlayOffMatches().keySet());
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
            final Map<Set<Player>, GameEnd> matchMap = scenario.chooseMatchMap(openMatch);
            findGame(matchMap, scenario, openMatch, players).ifPresent(game -> {
                game.getSetGenerator().setMid(openMatch.getMid());
                final PlayHook hook = scenario.getHooksOnMatches().getOrDefault(players,
                        PlayHook.builder()
                                .type(Hook.NonStop)
                                .callback((a, b) -> HookDecision.Score)
                                .build());
                final MatchMetaInfo matchMetaInfo = MatchMetaInfo.builder()
                        .openMatch(openMatch)
                        .players(players).build();
                scenario.getOnBeforeAnyMatch().forEach(hk -> hk.accept(scenario, matchMetaInfo));
                final HookDecision hookDecision = hook.pauseBefore(scenario, matchMetaInfo);
                ++completedMatches[0];
                log.info("Match id {} outcome {}", openMatch.getMid(), game);
                final Optional<SetScoreResultName> result = completeMatch(players, scenario, openMatch, game, hookDecision);
                if (hook.getType() == AfterScore ||
                        hook.getType() == AfterMatch && result.equals(Optional.of(SetScoreResultName.MatchComplete))) {
                    hook.pauseAfter(scenario, matchMetaInfo);
                }
            });
        }
    }

    private Optional<SetScoreResultName> completeMatch(Set<Player> players, TournamentScenario scenario,
            OpenMatchForJudge openMatch,
            GameEnd game, HookDecision hookDecision) {
        switch (hookDecision) {
            case Skip:
                return empty();
            case Score:
                return Optional.of(scoreSet(players, scenario, openMatch, game));
            default:
                throw new IllegalArgumentException("Unknown decision " + hookDecision);
        }
    }

    private SetScoreResultName scoreSet(Set<Player> players, TournamentScenario scenario,
            OpenMatchForJudge openMatch, GameEnd game) {
        final Map<Player, Integer> setOutcome = game.getSetGenerator().generate(scenario);
        final int ordNumber = game.getSetGenerator().getSetNumber() - 1;
        if (setOutcome.size() == 1) {
            Player resigningPlayer = setOutcome.keySet().stream().findFirst().get();
            rest.voidPost(TOURNAMENT_RESIGN, scenario.getPlayersSessions().get(resigningPlayer),
                    scenario.getTid());
            log.info("Player {} resigned in match with {}", resigningPlayer, players);
            scenario.chooseMatchMap(openMatch).remove(players);
            return SetScoreResultName.MatchComplete;
        }
        final Response response = rest.post(SCORE_SET, testAdmin,
                FinalMatchScore.builder()
                        .tid(scenario.getTid())
                        .setOrdNumber(ordNumber)
                        .mid(openMatch.getMid())
                        .scores(asList(
                                IdentifiedScore.builder()
                                        .score(setOutcome.get(
                                                game.getParticipants().get(0)))
                                        .uid(scenario.getPlayersSessions()
                                                .get(game.getParticipants()
                                                        .get(0)).getUid())
                                        .build(),
                                IdentifiedScore.builder()
                                        .score(setOutcome.get(
                                                game.getParticipants().get(1)))
                                        .uid(scenario.getPlayersSessions()
                                                .get(game.getParticipants()
                                                        .get(1)).getUid())
                                        .build()))
                        .build());

        switch (response.readEntity(SetScoreResult.class).getScoreOutcome()) {
            case LastMatchComplete:
            case MatchComplete:
                assertTrue("Match " + openMatch.getMid()
                                + " ended before its set generator",
                        game.getSetGenerator().isEmpty());
                scenario.chooseMatchMap(openMatch).remove(players);
                return SetScoreResultName.MatchComplete;
            case MatchContinues:
                assertFalse("Match " + openMatch.getMid()
                        + " continues while its set generator is empty",
                        game.getSetGenerator().isEmpty());
                return SetScoreResultName.MatchContinues;
            default:
                throw new IllegalStateException("Unknown match event");
        }
    }

    private Optional<GameEnd> findGame(Map<Set<Player>, GameEnd> matchMap,
            TournamentScenario scenario,
            OpenMatchForJudge openMatch,
            Set<Player> players) {
        final GameEnd gameEnd = matchMap.get(players);
        if (gameEnd == null) {
            if (scenario.getAutoResolution().isPresent()) {
                Iterator<Player> iterator = players.iterator();
                return Optional.of(GameEnd.game(iterator.next(),
                        scenario.getAutoResolution().get().choose(players),
                        iterator.next(), scenario.getRules().getMatch()));
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

    private void setupEnvironment(TournamentScenario scenario) {
        rest.voidAnonymousPost(DEV_CLEAN_SIGN_IN_TOKEN_TABLE, "");
        final String prefix = scenario.getName()
                .orElseGet(() -> "todo")
                + " " + valueGenerator.genName(8);
        if (scenario.getTestAdmin() == null) {
            testAdmin = userSessionGenerator.generate(prefix + " admin", UserType.Admin);
            scenario.setTestAdmin(testAdmin);
        } else {
            testAdmin = scenario.getTestAdmin();
        }
        restGenerator.generateSignInLinks(singletonList(testAdmin));
        final int countryId = daoGenerator.genCountry(prefix, testAdmin.getUid());
        final int cityId = daoGenerator.genCity(countryId, prefix, testAdmin.getUid());
        final int placeId = daoGenerator.genPlace(cityId, prefix,
                testAdmin.getUid(), scenario.getTables());
        scenario.setPlaceId(placeId);
        final int tid = daoGenerator.genTournament(prefix, testAdmin.getUid(),
                placeId, TournamentProps.builder()
                        .rules(scenario.getRules())
                        .state(TournamentState.Draft)
                        .build());
        scenario.setTid(tid);
        final Map<Player, TestUserSession> playersSession = scenario.getPlayersSessions();
        final List<Player> players = scenario.getPlayersByCategories().values()
                .stream().filter(p -> playersSession.get(p) == null)
                .sorted().collect(toList());
        final List<String> playerLabels = players.stream()
                .map(p -> "_p" + p.getNumber())
                .collect(toList());
        final List<TestUserSession> userSessions = userSessionGenerator
                .generateUserSessions(prefix, playerLabels);
        final Iterator<TestUserSession> sessions = userSessions.iterator();
        assertEquals(players.size(), userSessions.size());
        restGenerator.generateSignInLinks(userSessions);
        playersSession.forEach((pl, ses) -> {
            if (ses == null) {
                return;
            }
            scenario.getUidPlayer().put(ses.getUid(), pl);
        });
        for (Player player : players) {
            TestUserSession user = sessions.next();
            playersSession.put(player, user);
            scenario.getUidPlayer().put(user.getUid(), player);
        }
        for (PlayerCategory category : scenario.getCategoryDbId().keySet()) {
            final int catId = daoGenerator.genCategory(prefix + " " + category, tid);
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
