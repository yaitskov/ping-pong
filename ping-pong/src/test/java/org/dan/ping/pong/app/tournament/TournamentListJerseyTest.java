package org.dan.ping.pong.app.tournament;

import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_G8Q2_S1A2G11;
import static org.dan.ping.pong.app.tournament.TournamentResource.CANCEL_TOURNAMENT;
import static org.dan.ping.pong.app.tournament.TournamentResource.EDITABLE_TOURNAMENTS;
import static org.dan.ping.pong.app.tournament.TournamentState.Canceled;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.tournament.TournamentJerseyTest.DigestList;
import org.dan.ping.pong.mock.simulator.Simulator;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class TournamentListJerseyTest extends AbstractSpringJerseyTest {
    @Inject
    private Simulator simulator;

    @Inject
    private TournamentService tournamentService;

    @Test
    public void listCancelledAdministrableTournamentForAWhile() {
        final TournamentScenario scenario = TournamentScenario
                .begin()
                .rules(RULES_G8Q2_S1A2G11)
                .doNotBegin()
                .name("listClosedTournament");
        simulator.simulate(scenario);
        myRest().voidPost(CANCEL_TOURNAMENT, scenario.getTestAdmin(), scenario.getTid());
        assertThat(myRest().get(EDITABLE_TOURNAMENTS + "/20",
                scenario.getTestAdmin(), DigestList.class),
                hasItem(allOf(
                        hasProperty("tid", is(scenario.getTid())),
                        hasProperty("state", is(Canceled)))));
        assertThat(myRest().get(EDITABLE_TOURNAMENTS + "/-1",
                scenario.getTestAdmin(), DigestList.class),
                not(hasItem(hasProperty("tid", is(scenario.getTid())))));
    }
}
