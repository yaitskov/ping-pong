package org.dan.ping.pong.app.tournament;

import static org.dan.ping.pong.app.bid.BidState.Quit;
import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_G2Q1_S3A2G11;
import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_G8Q1_S1A2G11;
import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_G8Q1_S3A2G11;
import static org.dan.ping.pong.app.tournament.TournamentResource.TOURNAMENT_RESIGN;
import static org.dan.ping.pong.mock.simulator.FixedSetGenerator.game;
import static org.dan.ping.pong.mock.simulator.Hook.AfterMatch;
import static org.dan.ping.pong.mock.simulator.HookDecision.Score;
import static org.dan.ping.pong.mock.simulator.HookDecision.Skip;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.Player.p4;
import static org.dan.ping.pong.mock.simulator.Player.p5;
import static org.dan.ping.pong.mock.simulator.Player.p6;
import static org.dan.ping.pong.mock.simulator.Player.p7;
import static org.dan.ping.pong.mock.simulator.Player.p8;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableSet;
import lombok.RequiredArgsConstructor;
import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.match.ForTestBidDao;
import org.dan.ping.pong.app.match.ForTestMatchDao;
import org.dan.ping.pong.app.match.MatchDao;
import org.dan.ping.pong.mock.TestUserSession;
import org.dan.ping.pong.mock.simulator.HookCallback;
import org.dan.ping.pong.mock.simulator.HookDecision;
import org.dan.ping.pong.mock.simulator.MatchMetaInfo;
import org.dan.ping.pong.mock.simulator.PlayHook;
import org.dan.ping.pong.mock.simulator.Player;
import org.dan.ping.pong.mock.simulator.Simulator;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class TournamentResignJerseyTest extends AbstractSpringJerseyTest {
    @Inject
    private Simulator simulator;

    @Inject
    private ForTestBidDao bidDao;

    @Test
    public void resignInDraft() {
        final TournamentScenario scenario = TournamentScenario.begin()
                .doNotBegin()
                .name("resignInDraft")
                .rules(RULES_G8Q1_S1A2G11)
                .category(c1, p1, p2, p3);
        simulator.simulate(scenario);
        final Map<Player, TestUserSession> session = scenario.getPlayersSessions();
        myRest().voidPost(TOURNAMENT_RESIGN, session.get(p1), scenario.getTid());
        final Uid uid = session.get(p1).getUid();
        assertEquals(Optional.of(Quit), bidDao.getState(scenario.getTid(), uid));
    }

    @Inject
    private MatchDao matchDao;

    @Inject
    private ForTestMatchDao forTestMatchDao;

    @Test
    public void resignInGroupMiddle() {
        simulator.simulate(TournamentScenario.begin()
                .name("resignInGroupMiddle")
                .rules(RULES_G8Q1_S3A2G11)
                .category(c1, p1, p2, p3)
                .custom(game(p1, p3, -1, 0))
                .w30(p2, p3)
                .quitsGroup(p2)
                .champions(c1, p2));
    }

    @Test
    public void resignInLastGroupGame() {
        simulator.simulate(TournamentScenario.begin()
                .name("resignInLastGroupGame")
                .rules(RULES_G8Q1_S3A2G11)
                .category(c1, p1, p2, p3)
                .w30(p1, p3)
                .w32(p2, p1)
                .custom(game(p3, p2, -1, 0))
                .quitsGroup(p2)
                .champions(c1, p2));
    }

    @RequiredArgsConstructor
    private class ResignAfter2 implements HookCallback {
        private final int resignAfter;
        private final Player player;
        private int counter;

        @Override
        public HookDecision apply(TournamentScenario s, MatchMetaInfo metaInfo) {
            if (++counter == resignAfter) {
                myRest().voidPost(TOURNAMENT_RESIGN,
                        s.getPlayersSessions().get(player), s.getTid());
            }
            return Score;
        }
    }

    @Test
    public void resignAfterAllOwnGroupGames() {
        final ResignAfter2 resignAfter2  = new ResignAfter2(2, p1);
        simulator.simulate(TournamentScenario.begin()
                .name("quitAfterAllOwnGroup")
                .category(c1, p1, p2, p3)
                .rules(RULES_G8Q1_S3A2G11)
                .w30(p1, p2)
                .w32(p2, p3)
                .w30(p1, p3)
                .quitsGroup(p1)
                .pause(p1, p2, PlayHook.builder()
                        .type(AfterMatch)
                        .callback(resignAfter2)
                        .build())
                .pause(p1, p3, PlayHook.builder()
                        .type(AfterMatch)
                        .callback(resignAfter2)
                        .build())
                .champions(c1, p1));
    }

    @Test
    public void resignAfterAllOwnGroupGamesWinsCountFirst() {
        final ResignAfter2 resignAfter2  = new ResignAfter2(2, p1);
        final TournamentScenario scenario = TournamentScenario.begin()
                .name("quitAfterAllOwnGroupL")
                .rules(RULES_G8Q1_S3A2G11)
                .category(c1, p1, p2, p3)
                .w30(p1, p2)
                .w32(p2, p3)
                .w32(p1, p3)
                .quitsGroup(p1)
                .pause(p1, p2, PlayHook.builder()
                        .type(AfterMatch)
                        .callback(resignAfter2)
                        .build())
                .pause(p1, p3, PlayHook.builder()
                        .type(AfterMatch)
                        .callback(resignAfter2)
                        .build())
                .champions(c1, p1);

        simulator.simulate(scenario);
    }

    @Test
    public void resignFromActiveMatchForGold() {
        simulator.simulate(TournamentScenario.begin()
                .name("resignFromActiveGold")
                .rules(RULES_G2Q1_S3A2G11)
                .category(c1, p1, p2, p3, p4)
                .w30(p1, p2)
                .w32(p3, p4)
                .quitsGroup(p1, p3)
                .custom(game(p1, p3, -1, 0))
                .champions(c1, p3, p1));
    }

    @Test
    public void resignFromActiveFirstPlayOffMatch() {
        simulator.simulate(TournamentScenario.begin()
                .name("resignFromActiveFirst")
                .category(c1, p1, p2, p3, p4, p5, p6, p7, p8)
                .rules(RULES_G2Q1_S3A2G11)
                .w30(p1, p2)
                .w30(p3, p4)
                .w30(p5, p6)
                .w30(p7, p8)
                .quitsGroup(p1, p3, p5, p7)
                .custom(game(p1, p7, -1, 0))
                .w32(p5, p3)
                .w32(p5, p7)
                .champions(c1, p5, p7));
    }

    class ResignCallback implements HookCallback {
        @Override
        public HookDecision apply(TournamentScenario scenario, MatchMetaInfo metaInfo) {
            myRest().voidPost(TOURNAMENT_RESIGN, scenario.getPlayersSessions().get(p1), scenario.getTid());
            return Skip;
        }
    }

    @Test
    public void resignFromPlayOffMatchWithoutKnownOpponent() {
        simulator.simulate(TournamentScenario.begin()
                .name("resignFromUnknown")
                .rules(RULES_G2Q1_S3A2G11)
                .category(c1, p1, p2, p3, p4, p5, p6, p7, p8)
                .w30(p1, p2)
                .w30(p3, p4)
                .w30(p5, p6)
                .w30(p7, p8)
                .quitsGroup(p1, p3, p5, p7)
                .w32(p5, p3)
                .w32(p7, p5)
                .champions(c1, p7, p5)
                .pause(p1, p2, PlayHook.builder()
                        .type(AfterMatch)
                        .callback(new ResignCallback())
                        .build()));
    }

    @Test
    public void resignFromPassivePlayOffMatch() {
        simulator.simulate(TournamentScenario.begin()
                .name("resignFromPassive")
                .rules(RULES_G2Q1_S3A2G11)
                .category(c1, p1, p2, p3, p4, p5, p6, p7, p8)
                .w30(p1, p2)
                .w30(p3, p4)
                .w30(p5, p6)
                .w30(p7, p8)
                .quitsGroup(p1, p3, p5, p7)
                .w32(p5, p3)
                .w32(p7, p5)
                .champions(c1, p7, p5)
                .pause(p3, p4, PlayHook.builder()
                        .type(AfterMatch)
                        .callback((s, meta) -> {
                            assertThat(s.getGroupMatches().keySet(),
                                    allOf(not(hasItem(ImmutableSet.of(p1, p2))),
                                            not(hasItem(ImmutableSet.of(p3, p4)))));
                            new ResignCallback().apply(s, meta);
                            return Score;
                        })
                        .build()));
    }
    // resign too many from group so left participants is less than quits from group
    // chain resign a b => b resigns but a c (resigned earlier) so => a goes to next level with d automatically
}
