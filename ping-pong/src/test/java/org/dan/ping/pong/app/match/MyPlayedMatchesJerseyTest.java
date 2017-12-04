package org.dan.ping.pong.app.match;

import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_G8Q2_S3A2G11;
import static org.dan.ping.pong.app.match.MatchResource.MATCH_LIST_PLAYED_ME;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.AllOf.allOf;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.tournament.JerseyWithSimulator;
import org.dan.ping.pong.mock.simulator.Simulator;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;

import javax.inject.Inject;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class MyPlayedMatchesJerseyTest extends AbstractSpringJerseyTest {
    @Inject
    private Simulator simulator;

    @Test
    public void myPlayedMatches() {
        final TournamentScenario scenario = TournamentScenario.begin()
                .tables(1)
                .name("myPlayedMatches")
                .rules(RULES_G8Q2_S3A2G11)
                .category(c1, p1, p2, p3)
                .win(p1, p2)
                .win(p1, p3)
                .win(p2, p3)
                .quitsGroup(p1, p2)
                .win(p2, p1)
                .champions(c1, p2, p1);
        simulator.simulate(scenario);

        final PlayedMatchList played = myRest()
                .get(MATCH_LIST_PLAYED_ME + scenario.getTid().getTid(),
                        scenario.getPlayersSessions().get(p2),
                        PlayedMatchList.class);

        assertThat(played.getProgress(),
                allOf(hasProperty("totalMatches", is(3L)),
                        hasProperty("leftMatches", is(0L))));

        assertThat(played.getPlayOff(), contains(
                allOf(
                        hasProperty("opponent",
                                hasProperty("uid", is(scenario.player2Uid(p1)))),
                        hasProperty("winnerUid", is(Optional.of(scenario.player2Uid(p2)))))));

        assertThat(played.getInGroup(), contains(
                allOf(
                        hasProperty("opponent",
                                hasProperty("uid", is(scenario.player2Uid(p1)))),
                        hasProperty("winnerUid", is(Optional.of(scenario.player2Uid(p1))))),
                allOf(
                        hasProperty("opponent",
                                hasProperty("uid", is(scenario.player2Uid(p3)))),
                        hasProperty("winnerUid", is(Optional.of(scenario.player2Uid(p2)))))));
    }
}
