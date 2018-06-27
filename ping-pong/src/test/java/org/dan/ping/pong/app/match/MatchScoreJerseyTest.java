package org.dan.ping.pong.app.match;

import static java.util.Arrays.asList;
import static org.dan.ping.pong.app.match.MatchResource.MATCH_FIND_BY_PARTICIPANTS;
import static org.dan.ping.pong.app.match.MatchResource.SCORE_SET;
import static org.dan.ping.pong.app.tournament.ParticipantMemState.FILLER_LOSER_BID;
import static org.dan.ping.pong.app.tournament.TournamentRulesConst.RULES_JP_S1A2G11;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.tournament.JerseyWithSimulator;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.dan.ping.pong.mock.simulator.imerative.ImperativeSimulator;
import org.dan.ping.pong.mock.simulator.imerative.ImperativeSimulatorFactory;
import org.dan.ping.pong.sys.error.Error;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class MatchScoreJerseyTest extends AbstractSpringJerseyTest {
    @Inject
    private ImperativeSimulatorFactory isf;

    @Test
    public void scoreMatchWithByeFails() {
        final TournamentScenario scenario = TournamentScenario.begin()
                .name("matchWithBye")
                .rules(RULES_JP_S1A2G11)
                .category(c1, p1, p2, p3);
        final ImperativeSimulator simulator = isf.create(scenario);
        simulator.run(ImperativeSimulator::beginTournament);

        final int tid = scenario.getTid().getTid();
        final Bid bid1 = scenario.player2Bid(p1);
        final Mid mid = myRest().get(MATCH_FIND_BY_PARTICIPANTS + tid + "/"
                        + bid1.intValue() + "/" + FILLER_LOSER_BID.intValue(),
                new GenericType<List<Mid>>(){}).get(0);
        final Response response = myRest().post(
                SCORE_SET,
                scenario.getTestAdmin(),
                SetScoreReq
                        .builder()
                        .mid(mid)
                        .tid(scenario.getTid())
                        .setOrdNumber(0)
                        .scores(asList(
                                IdentifiedScore.builder()
                                        .bid(bid1).score(11).build(),
                                IdentifiedScore.builder()
                                        .bid(FILLER_LOSER_BID).score(0).build()))
                        .build());

        assertThat(response.readEntity(Error.class),
                hasProperty("message", is("Match is not in a scorable state")));
        assertThat(response, hasProperty("status", is(400)));
    }
}
