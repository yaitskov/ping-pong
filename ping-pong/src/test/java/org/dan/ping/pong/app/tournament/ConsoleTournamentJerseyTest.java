package org.dan.ping.pong.app.tournament;

import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_G8Q1_S1A2G11;
import static org.dan.ping.pong.app.tournament.TournamentResource.GET_TOURNAMENT_RULES;
import static org.dan.ping.pong.app.tournament.TournamentResource.TOURNAMENT_CONSOLE_CREATE;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.dan.ping.pong.mock.simulator.TournamentScenario.begin;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.dan.ping.pong.mock.simulator.imerative.ImperativeSimulatorFactory;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;

import javax.inject.Inject;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class ConsoleTournamentJerseyTest extends AbstractSpringJerseyTest {
    @Inject
    private ImperativeSimulatorFactory isf;

    @Test
    public void createConsoleTournamentForOpenTournament() {
        final TournamentScenario scenario = begin().name("withConsole")
                .rules(RULES_G8Q1_S1A2G11.withPlace(Optional.empty()))
                .category(c1, p1, p2, p3);
        isf.create(scenario)
                .run(c -> {
                    c.beginTournament();
                    final Tid consoleTid = myRest()
                            .post(TOURNAMENT_CONSOLE_CREATE, scenario, scenario.getTid())
                            .readEntity(Tid.class);

                    assertThat(consoleTid, greaterThan(scenario.getTid()));
                    assertThat(myRest().get(GET_TOURNAMENT_RULES + consoleTid.getTid(), TournamentRules.class),
                            allOf(hasProperty("group", is(Optional.empty())),
                                    hasProperty("match", is(RULES_G8Q1_S1A2G11.getMatch())),
                                    hasProperty("playOff", is(RULES_G8Q1_S1A2G11.getPlayOff()))));
                });
    }
}
