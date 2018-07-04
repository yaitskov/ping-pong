package org.dan.ping.pong.app.suggestion;

import static org.dan.ping.pong.app.suggestion.ParticipantSuggestionResource.PARTICIPANT_SUGGESTION;
import static org.dan.ping.pong.app.tournament.TournamentRulesConst.RULES_G3Q2_S1A2G11;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.dan.ping.pong.mock.simulator.TournamentScenario.begin;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.tournament.JerseyWithSimulator;
import org.dan.ping.pong.app.user.UserLink;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.dan.ping.pong.mock.simulator.imerative.ImperativeSimulator;
import org.dan.ping.pong.mock.simulator.imerative.ImperativeSimulatorFactory;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.core.GenericType;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class SuggestionResourceJerseyTest extends AbstractSpringJerseyTest {
    @Inject
    private ImperativeSimulatorFactory isf;

    @Test
    public void suggestionGetsEnlistedParticipant() {
        final TournamentScenario scenario = begin()
                .name("suggestionExtends")
                .rules(RULES_G3Q2_S1A2G11)
                .category(c1, p1, p2);

        final ImperativeSimulator simulator = isf.create(scenario);
        simulator.run(ImperativeSimulator::beginTournament);
        final String p1 = namePart();
        final String p2 = namePart();
        final String name = p1 + " " + p2;
        simulator.enlistParticipant(scenario, scenario.catId(c1), Optional.empty(), name);

        final List<UserLink> suggestedUsers = myRest()
                .post(PARTICIPANT_SUGGESTION, scenario,
                        SuggestReq.builder()
                                .pattern(p2.substring(0, 3) + " " + p1.substring(0, 3))
                                .page(PageAdr.ofSize(10))
                                .build())
                .readEntity(new GenericType<List<UserLink>>(){});
        assertThat(suggestedUsers, hasItem(allOf(
                hasProperty("uid"),
                hasProperty("name", is(name)))));
    }

    public String namePart() {
        return UUID.randomUUID().toString().substring(0, 6);
    }
}
