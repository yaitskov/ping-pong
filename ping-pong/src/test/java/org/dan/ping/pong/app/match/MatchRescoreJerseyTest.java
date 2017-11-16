package org.dan.ping.pong.app.match;

import static java.util.Arrays.asList;
import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_G_S3A2G11;
import static org.dan.ping.pong.mock.simulator.FixedSetGenerator.game;
import static org.dan.ping.pong.mock.simulator.HookDecision.Score;
import static org.dan.ping.pong.mock.simulator.HookDecision.Skip;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.dan.ping.pong.app.match.MatchEditorService.DONT_CHECK_HASH;

import com.google.common.collect.ImmutableMap;
import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.tournament.JerseyWithSimulator;
import org.dan.ping.pong.mock.simulator.Hook;
import org.dan.ping.pong.mock.simulator.MatchMetaInfo;
import org.dan.ping.pong.mock.simulator.PlayHook;
import org.dan.ping.pong.mock.simulator.Simulator;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class MatchRescoreJerseyTest extends AbstractSpringJerseyTest {
    @Inject
    private Simulator simulator;

    @Test
    public void rescoreCompleteMatchGroup2NoPlayOff() {
        MatchMetaInfo[] mm = new MatchMetaInfo[1];
        final TournamentScenario scenario = TournamentScenario.begin()
                .name("rescoreComMtcG2PO2")
                .rules(RULES_G_S3A2G11)
                .category(c1, p1, p2)
                .custom(game(p1, p2, 0, 11, 1, 11, 2, 11))
                .pause(p1, p2, PlayHook.builder()
                        .type(Hook.AfterMatch)
                        .callback((s, m) -> {
                            if (mm[0] == null) {
                                mm[0] = m;
                                simulator.rescore(s,
                                        m.getOpenMatch().getMid(),
                                        ImmutableMap.of(p1, asList(11, 11, 11),
                                                p2, asList(3, 4, 5)), DONT_CHECK_HASH);
                            }
                            return Skip;
                        }).build())
                .quitsGroup(p1)
                .champions(c1, p1);

        simulator.simulate(scenario);
    }

    @Test
    public void rescoreKeepOpenGroupMatchNoPlayOff2Matches() {
        final int[] c = new int[1];
        final TournamentScenario scenario = TournamentScenario.begin()
                .name("rescoreKeepOpenMtchGrpNPO2")
                .rules(RULES_G_S3A2G11)
                .category(c1, p1, p2)
                .custom(game(p2, p1, 11, 0, 11, 1, 2, 11))
                .pause(p1, p2, PlayHook.builder()
                        .type(Hook.BeforeScore)
                        .callback((s, m) -> {
                            if (++c[0] == 3) {
                                simulator.rescore(s,
                                        m.getOpenMatch().getMid(),
                                        ImmutableMap.of(p2, asList(0, 1, 2),
                                                p1, asList(11, 11, 11)), DONT_CHECK_HASH);
                            }
                            return Score;
                        }).build())
                .quitsGroup(p1)
                .champions(c1, p1);

        simulator.simulate(scenario);
    }

    @Test
    public void rescoreCompleteNotLastMatchNoPlayOff3Matches() {
        MatchMetaInfo[] p1p2m = new MatchMetaInfo[1];
        MatchMetaInfo[] mm = new MatchMetaInfo[1];
        final TournamentScenario scenario = TournamentScenario.begin()
                .name("rescoreCompleteNotLastGrpMtchNPO3")
                .rules(RULES_G_S3A2G11)
                .category(c1, p1, p2, p3)
                .custom(game(p1, p3, 11, 0, 11, 1, 11, 2))
                .custom(game(p2, p1, 11, 0, 11, 1, 11, 2))
                .pause(p1, p2, PlayHook.builder()
                        .type(Hook.AfterMatch)
                        .callback((s, m) -> {
                            p1p2m[0] = m;
                            return Score;
                        })
                        .build())
                .custom(game(p2, p3, 11, 0, 11, 1, 11, 2))
                .pause(p2, p3, PlayHook.builder()
                        .type(Hook.AfterScore)
                        .callback((s, m) -> {
                            if (mm[0] == null) {
                                mm[0] = m;
                                simulator.rescore(s,
                                        p1p2m[0].getOpenMatch().getMid(),
                                        ImmutableMap.of(p2, asList(0, 0, 0),
                                                p1, asList(11, 11, 11)),
                                        DONT_CHECK_HASH);
                            }
                            return Score;
                        }).build())
                .quitsGroup(p1)
                .champions(c1, p1);

        simulator.simulate(scenario);
    }
}
