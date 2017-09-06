package org.dan.ping.pong.app.tournament;

import static org.dan.ping.pong.app.bid.BidResource.BID_SET_STATE;
import static org.dan.ping.pong.app.bid.BidState.Here;
import static org.dan.ping.pong.app.bid.BidState.Paid;
import static org.dan.ping.pong.app.tournament.TournamentResource.BEGIN_TOURNAMENT;
import static org.dan.ping.pong.app.tournament.TournamentResource.TOURNAMENT_RESIGN;
import static org.dan.ping.pong.mock.AdminSessionGenerator.ADMIN_SESSION;
import static org.dan.ping.pong.mock.simulator.EnlistMode.Enlist;
import static org.dan.ping.pong.mock.simulator.EnlistMode.Pay;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.Player.p4;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.dan.ping.pong.mock.simulator.SimulatorParams.T_1_Q_1_G_8;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.bid.BidDao;
import org.dan.ping.pong.app.bid.SetBidState;
import org.dan.ping.pong.app.castinglots.UncheckedParticipantsError;
import org.dan.ping.pong.mock.TestAdmin;
import org.dan.ping.pong.mock.TestUserSession;
import org.dan.ping.pong.mock.simulator.Simulator;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.dan.ping.pong.util.time.Clocker;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Response;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class TournamentBeginJerseyTest extends AbstractSpringJerseyTest {
    @Inject
    private Simulator simulator;

    @Inject
    private TournamentService tournamentService;

    @Inject
    private BidDao bidDao;

    @Inject
    @Named(ADMIN_SESSION)
    private TestAdmin session;

    @Inject
    private Clocker clocker;

    @Test
    public void failsToBeginUntilThereAreUncheckedParticipants() {
        final TournamentScenario scenario = TournamentScenario.begin()
                .doNotBegin()
                .name("failDueUnchecked")
                .presence(Enlist, p1)
                .presence(Pay, p2)
                .category(c1, p1, p2, p3, p4);

        simulator.simulate(T_1_Q_1_G_8, scenario);
        TestUserSession session1 = scenario.getPlayersSessions().get(p1);
        final int uid1 = session1.getUid();
        TestUserSession session2 = scenario.getPlayersSessions().get(p2);
        final int uid2 = session2.getUid();
        final Response re = myRest().post(BEGIN_TOURNAMENT, scenario.getTestAdmin().getSession(), scenario.getTid());
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

        assertEquals(204, myRest().post(BEGIN_TOURNAMENT,
                scenario.getTestAdmin().getSession(), scenario.getTid())
                .getStatus());
    }
}
