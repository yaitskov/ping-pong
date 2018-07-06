package org.dan.ping.pong.app.tournament;

import static org.dan.ping.pong.app.bid.BidResource.BID_SET_STATE;
import static org.dan.ping.pong.app.bid.BidState.Here;
import static org.dan.ping.pong.app.bid.BidState.Paid;
import static org.dan.ping.pong.app.tournament.ResignTournament.resignOfTid;
import static org.dan.ping.pong.app.tournament.TournamentResource.BEGIN_TOURNAMENT;
import static org.dan.ping.pong.app.tournament.TournamentResource.TOURNAMENT_RESIGN;
import static org.dan.ping.pong.app.tournament.TournamentRulesConst.RULES_G8Q1_S1A2G11;
import static org.dan.ping.pong.app.tournament.TournamentService.PLACE_IS_BUSY;
import static org.dan.ping.pong.app.tournament.TournamentService.TID;
import static org.dan.ping.pong.mock.simulator.EnlistMode.Enlist;
import static org.dan.ping.pong.mock.simulator.EnlistMode.Pay;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.Player.p4;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.bid.SetBidState;
import org.dan.ping.pong.app.castinglots.UncheckedParticipantsError;
import org.dan.ping.pong.mock.TestUserSession;
import org.dan.ping.pong.mock.simulator.Simulator;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.dan.ping.pong.sys.error.TemplateError;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class TournamentBeginJerseyTest extends AbstractSpringJerseyTest {
    @Inject
    private Simulator simulator;

    @Test
    public void failsToBeginUntilThereAreUncheckedParticipants() {
        final TournamentScenario scenario = TournamentScenario.begin()
                .doNotBegin()
                .rules(RULES_G8Q1_S1A2G11)
                .name("failDueUnchecked")
                .category(c1, p1, p2, p3, p4)
                .presence(Enlist, p1)
                .presence(Pay, p2);

        simulator.simulate(scenario);
        TestUserSession session1 = scenario.findSession(p1);
        final Bid bid1 = session1.getCatBid().get(c1);
        TestUserSession session2 = scenario.findSession(p2);
        final Bid bid2 = session2.getCatBid().get(c1);
        final Response re = myRest().post(BEGIN_TOURNAMENT,
                scenario.getTestAdmin().getSession(), scenario.getTid());
        assertEquals(400, re.getStatus());
        assertThat(
                re.readEntity(UncheckedParticipantsError.class),
                hasProperty("users", hasItems(
                        hasProperty("bid", is(bid1)),
                        hasProperty("bid", is(bid2)))));


        myRest().voidPost(TOURNAMENT_RESIGN, session1, resignOfTid(scenario.getTid()));

        assertEquals(400, myRest().post(BEGIN_TOURNAMENT,
                scenario.getTestAdmin().getSession(), scenario.getTid())
                .getStatus());

        myRest().voidPost(BID_SET_STATE, scenario.getTestAdmin(),
                SetBidState.builder()
                        .expected(Paid)
                        .target(Here)
                        .tid(scenario.getTid())
                        .bid(bid2)
                        .build());

        myRest().voidPost(BEGIN_TOURNAMENT, scenario.getTestAdmin(), scenario.getTid());
    }

    @Test
    public void failDuePlaceIsBusy() {
        final TournamentScenario scenario1 = TournamentScenario.begin()
                .doNotBegin()
                .rules(RULES_G8Q1_S1A2G11)
                .name("failDuePlaceIsBusy1")
                .category(c1, p1, p2);
        simulator.simulate(scenario1);

        final TournamentScenario scenario2 = TournamentScenario.begin()
                .doNotBegin()
                .rules(RULES_G8Q1_S1A2G11)
                .name("failDuePlaceIsBusy2")
                .category(c1, p1, p2);
        scenario2.setTestAdmin(scenario1.getTestAdmin());
        scenario2.setPlaceId(scenario1.getPlaceId());
        simulator.simulate(scenario2);

        myRest().voidPost(BEGIN_TOURNAMENT, scenario1, scenario1.getTid());
        final Response response = myRest().post(BEGIN_TOURNAMENT,
                scenario1.getTestAdmin().getSession(), scenario2.getTid());
        assertEquals(400, response.getStatus());
        assertThat(
                response.readEntity(TemplateError.class),
                allOf(
                        hasProperty("message", is(PLACE_IS_BUSY)),
                        hasProperty("params", hasEntry(TID, scenario1.getTid().getTid()))));
    }
}
