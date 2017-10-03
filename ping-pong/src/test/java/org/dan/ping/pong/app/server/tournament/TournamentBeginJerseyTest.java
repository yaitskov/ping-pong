package org.dan.ping.pong.app.server.tournament;

import static org.dan.ping.pong.app.server.bid.BidResource.BID_SET_STATE;
import static org.dan.ping.pong.app.server.bid.BidState.Here;
import static org.dan.ping.pong.app.server.bid.BidState.Paid;
import static org.dan.ping.pong.app.server.castinglots.CastingLotsService.N;
import static org.dan.ping.pong.app.server.castinglots.CastingLotsService.NOT_ENOUGH_PARTICIPANTS;
import static org.dan.ping.pong.app.server.match.MatchJerseyTest.RULES_G8Q1_S1A2G11;
import static org.dan.ping.pong.app.server.tournament.TournamentResource.BEGIN_TOURNAMENT;
import static org.dan.ping.pong.app.server.tournament.TournamentResource.TOURNAMENT_RESIGN;
import static org.dan.ping.pong.app.server.tournament.TournamentService.PLACE_IS_BUSY;
import static org.dan.ping.pong.app.server.tournament.TournamentService.TID;
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
import org.dan.ping.pong.app.server.bid.SetBidState;
import org.dan.ping.pong.app.server.castinglots.UncheckedParticipantsError;
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
                .presence(Enlist, p1)
                .presence(Pay, p2)
                .category(c1, p1, p2, p3, p4);

        simulator.simulate(scenario);
        TestUserSession session1 = scenario.getPlayersSessions().get(p1);
        final int uid1 = session1.getUid();
        TestUserSession session2 = scenario.getPlayersSessions().get(p2);
        final int uid2 = session2.getUid();
        final Response re = myRest().post(BEGIN_TOURNAMENT,
                scenario.getTestAdmin().getSession(), scenario.getTid());
        assertEquals(400, re.getStatus());
        assertThat(
                re.readEntity(UncheckedParticipantsError.class),
                hasProperty("users", hasItems(
                        hasProperty("uid", is(uid1)),
                        hasProperty("uid", is(uid2)))));


        myRest().voidPost(TOURNAMENT_RESIGN, session1, scenario.getTid());

        assertEquals(400, myRest().post(BEGIN_TOURNAMENT,
                scenario.getTestAdmin().getSession(), scenario.getTid())
                .getStatus());

        myRest().voidPost(BID_SET_STATE, scenario.getTestAdmin(),
                SetBidState.builder()
                        .expected(Paid)
                        .target(Here)
                        .tid(scenario.getTid())
                        .uid(uid2)
                        .build());

        myRest().voidPost(BEGIN_TOURNAMENT, scenario.getTestAdmin(), scenario.getTid());
    }

    @Test
    public void failedToBeginDueNotEnoughParticipants() {
        final TournamentScenario scenario = TournamentScenario.begin()
                .doNotBegin()
                .rules(RULES_G8Q1_S1A2G11)
                .name("failBeginNoParticipants")
                .category(c1, p1);

        simulator.simulate(scenario);

        final Response response = myRest().post(BEGIN_TOURNAMENT,
                scenario.getTestAdmin().getSession(), scenario.getTid());
        assertEquals(400, response.getStatus());
        assertThat(
                response.readEntity(TemplateError.class),
                allOf(
                        hasProperty("message", is(NOT_ENOUGH_PARTICIPANTS)),
                        hasProperty("params", hasEntry(N, 1))));
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
                        hasProperty("params", hasEntry(TID, scenario1.getTid()))));
    }
}
