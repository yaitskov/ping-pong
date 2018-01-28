package org.dan.ping.pong.app.tournament;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_G8Q2_S1A2G11;
import static org.dan.ping.pong.app.tournament.Tid.TOURNAMENT_ID_SHOULD_BE_A_POSITIVE_NUMBER;
import static org.dan.ping.pong.app.tournament.TournamentResource.TOURNAMENT_INVALIDATE_CACHE;
import static org.dan.ping.pong.sys.validation.TidBodyRequired.REQUEST_BODY_MUST_BE_A_TOURNAMENT_ID;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.mock.simulator.Simulator;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.dan.ping.pong.sys.error.Error;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class JerseyValidationTest extends AbstractSpringJerseyTest {
    @Inject
    private Simulator simulator;

    @Test
    public void failInJsonDueTidIsNull() {
        final TournamentScenario scenario = TournamentScenario
                .begin()
                .rules(RULES_G8Q2_S1A2G11)
                .doNotBegin()
                .name("failDueTidIsNull");
        simulator.simulate(scenario);
        final Response response = myRest().post(
                TOURNAMENT_INVALIDATE_CACHE, scenario.getTestAdmin(), "");
        assertThat(response, allOf(
                hasProperty("status", is(400)),
                hasProperty("mediaType", is(APPLICATION_JSON_TYPE))));
        assertThat(response.readEntity(Error.class),
                hasProperty("message", is(REQUEST_BODY_MUST_BE_A_TOURNAMENT_ID)));
        assertThat(myRest().post(TOURNAMENT_INVALIDATE_CACHE, scenario.getTestAdmin(), "-1")
                        .readEntity(Error.class),
                hasProperty("message", is(TOURNAMENT_ID_SHOULD_BE_A_POSITIVE_NUMBER)));
    }
}
