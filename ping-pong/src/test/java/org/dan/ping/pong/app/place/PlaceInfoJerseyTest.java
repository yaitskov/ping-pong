package org.dan.ping.pong.app.place;

import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_G2Q1_S1A2G11;
import static org.dan.ping.pong.app.place.PlaceResource.PLACE_INFO;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.tournament.JerseyWithSimulator;
import org.dan.ping.pong.mock.simulator.Simulator;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class PlaceInfoJerseyTest extends AbstractSpringJerseyTest {
    @Inject
    private Simulator simulator;

    @Test
    public void countTablesWith6OfThem() {
        final TournamentScenario scenario = TournamentScenario.begin()
                .doNotBegin()
                .tables(6)
                .rules(RULES_G2Q1_S1A2G11)
                .category(c1, p1)
                .name("count6Tables");
        simulator.simulate(scenario);

        final PlaceInfoCountTables placeInfo = myRest().get(PLACE_INFO + scenario.getPlaceId(),
                scenario, PlaceInfoCountTables.class);
        assertThat(placeInfo.getTables(), is(6));
    }
}
