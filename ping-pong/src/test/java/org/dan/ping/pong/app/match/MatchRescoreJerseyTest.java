package org.dan.ping.pong.app.match;

import static java.util.Arrays.asList;
import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_G_S3A2G11;
import static org.dan.ping.pong.mock.simulator.FixedSetGenerator.game;
import static org.dan.ping.pong.mock.simulator.HookDecision.Skip;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
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
    public void rescoreOpenGroupMatchNoPlayOff2Matches() {
        MatchMetaInfo[] mm = new MatchMetaInfo[1];
        final TournamentScenario scenario = TournamentScenario.begin()
                .name("rescoreOpenGrpMtchNPO2")
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
}
