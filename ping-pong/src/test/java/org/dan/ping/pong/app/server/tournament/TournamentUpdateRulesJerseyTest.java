package org.dan.ping.pong.app.server.tournament;

import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static org.dan.ping.pong.app.server.match.MatchJerseyTest.RULES_G2Q1_S3A2G11;
import static org.dan.ping.pong.app.server.match.MatchJerseyTest.RULES_G8Q1_S1A2G11;
import static org.dan.ping.pong.app.server.tournament.TournamentResource.TOURNAMENT_RULES;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.mock.simulator.Simulator;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class TournamentUpdateRulesJerseyTest extends AbstractSpringJerseyTest {
    @Inject
    private Simulator simulator;

    @Test
    public void updateRules() {
        final TournamentScenario scenario = TournamentScenario.begin()
                .doNotBegin()
                .rules(RULES_G8Q1_S1A2G11)
                .name("updateRules");

        simulator.simulate(scenario);

        assertThat(myRest().get(TOURNAMENT_RULES + "/" + scenario.getTid(),
                TournamentRules.class),
                hasProperty("group", optionalWithValue(hasProperty("groupSize", is(8)))));

        myRest().voidPost(TOURNAMENT_RULES, scenario.getTestAdmin(),
                TidIdentifiedRules.builder()
                        .rules(RULES_G2Q1_S3A2G11)
                        .tid(scenario.getTid())
                        .build());

        assertThat(myRest().get(TOURNAMENT_RULES + "/" + scenario.getTid(),
                TournamentRules.class),
                hasProperty("group", optionalWithValue(hasProperty("groupSize", is(2)))));
    }
}
