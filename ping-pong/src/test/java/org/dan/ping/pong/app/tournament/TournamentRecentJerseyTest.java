package org.dan.ping.pong.app.tournament;

import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_G8Q1_S1A2G11;
import static org.dan.ping.pong.app.tournament.TournamentResource.MY_RECENT_TOURNAMENT;
import static org.dan.ping.pong.app.tournament.TournamentResource.MY_RECENT_TOURNAMENT_JUDGEMENT;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.mock.TestUserSession;
import org.dan.ping.pong.mock.simulator.Simulator;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class TournamentRecentJerseyTest extends AbstractSpringJerseyTest {
    @Inject
    private Simulator simulator;

    @Test
    public void myRecentJudgeTournaments() {
        final TournamentScenario pastScenario = TournamentScenario.begin()
                .name("recentJudgePast")
                .rules(RULES_G8Q1_S1A2G11)
                .category(c1, p1, p2)
                .win(p1, p2)
                .quitsGroup(p1, p2)
                .champions(c1, p1);

        simulator.simulate(pastScenario);

        final TournamentScenario futureScenario = TournamentScenario.begin()
                .name("recentJudgeFuture")
                .rules(RULES_G8Q1_S1A2G11)
                .category(c1, p1, p2)
                .doNotBegin();

        futureScenario.setTestAdmin(pastScenario.getTestAdmin());
        simulator.simulate(futureScenario);

        final MyRecentJudgedTournaments r = myRest()
                .get(MY_RECENT_TOURNAMENT_JUDGEMENT,
                        pastScenario.getTestAdmin(),
                        MyRecentJudgedTournaments.class);

        assertEquals(pastScenario.getTid(), r.getPrevious().get().getTid());
        assertThat(r.getPrevious().get().getName(), notNullValue());
        assertEquals(futureScenario.getTid(), r.getNext().get().getTid());
        assertThat(r.getNext().get().getOpensAt(), notNullValue());
        assertThat(r.getNext().get().getName(), notNullValue());
    }

    @Test
    public void myRecentTournaments() {
        final TournamentScenario pastScenario = TournamentScenario.begin()
                .name("recentJudgePast")
                .rules(RULES_G8Q1_S1A2G11)
                .category(c1, p1, p2)
                .lose(p1, p2)
                .quitsGroup(p1, p2)
                .champions(c1, p2);

        simulator.simulate(pastScenario);

        final TournamentScenario futureScenario = TournamentScenario.begin()
                .name("recentJudgeFuture")
                .rules(RULES_G8Q1_S1A2G11)
                .category(c1, p1, p2)
                .doNotBegin();
        final TestUserSession userSession = pastScenario.getPlayersSessions().get(p1);

        futureScenario.getPlayersSessions().put(p1, userSession);

        simulator.simulate(futureScenario);

        final MyRecentTournaments r = myRest().get(MY_RECENT_TOURNAMENT,
                userSession, MyRecentTournaments.class);

        assertEquals(pastScenario.getTid(), r.getPrevious().get().getTid());
        assertEquals(BidState.Lost, r.getPrevious().get().getOutcome());
        assertThat(r.getPrevious().get().getName(), notNullValue());

        assertEquals(futureScenario.getTid(), r.getNext().get().getTid());
        assertThat(r.getNext().get().getOpensAt(), notNullValue());
        assertThat(r.getNext().get().getName(), notNullValue());
    }
}
