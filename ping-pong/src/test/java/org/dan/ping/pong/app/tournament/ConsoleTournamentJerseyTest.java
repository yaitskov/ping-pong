package org.dan.ping.pong.app.tournament;

import static org.dan.ping.pong.app.bid.BidState.Lost;
import static org.dan.ping.pong.app.bid.BidState.Play;
import static org.dan.ping.pong.app.bid.BidState.Wait;
import static org.dan.ping.pong.app.bid.BidState.Win1;
import static org.dan.ping.pong.app.bid.BidState.Win2;
import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_G8Q1_S1A2G11;
import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_G8Q1_S1A2G11_NP;
import static org.dan.ping.pong.app.tournament.TournamentResource.GET_TOURNAMENT_RULES;
import static org.dan.ping.pong.app.tournament.TournamentResource.TOURNAMENT_CONSOLE_CREATE;
import static org.dan.ping.pong.app.tournament.TournamentState.Open;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.Player.p4;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.dan.ping.pong.mock.simulator.TournamentScenario.begin;
import static org.dan.ping.pong.mock.simulator.imerative.BidStatesDesc.restState;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.dan.ping.pong.mock.simulator.imerative.ImperativeSimulator;
import org.dan.ping.pong.mock.simulator.imerative.ImperativeSimulatorFactory;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;

import javax.inject.Inject;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class ConsoleTournamentJerseyTest extends AbstractSpringJerseyTest {
    @Inject
    private ImperativeSimulatorFactory isf;

    @Test
    public void createConsoleTournamentForOpenTournament() {
        final TournamentScenario scenario = begin().name("withConsole")
                .rules(RULES_G8Q1_S1A2G11_NP)
                .category(c1, p1, p2, p3);
        isf.create(scenario)
                .run(c -> {
                    c.beginTournament();
                    final Tid consoleTid = myRest()
                            .post(TOURNAMENT_CONSOLE_CREATE, scenario, scenario.getTid())
                            .readEntity(Tid.class);

                    assertThat(consoleTid, greaterThan(scenario.getTid()));
                    assertThat(myRest().get(GET_TOURNAMENT_RULES + consoleTid.getTid(), TournamentRules.class),
                            allOf(hasProperty("group", is(Optional.empty())),
                                    hasProperty("match", is(RULES_G8Q1_S1A2G11.getMatch())),
                                    hasProperty("playOff", is(RULES_G8Q1_S1A2G11.getPlayOff()))));
                });
    }

    @Test
    public void consoleTourByDefaultRules3() {
        final TournamentScenario scenario = begin().name("consoleTourByDefaultRules3")
                .rules(RULES_G8Q1_S1A2G11_NP)
                .category(c1, p1, p2, p3);
        isf.create(scenario)
                .run(c -> {
                    final ImperativeSimulator console = c.beginTournament()
                            .createConsoleTournament()
                            .scoreSet(p1, 11, p2, 3)
                            .scoreSet(p2, 11, p3, 5)
                            .scoreSet(p1, 11, p3, 4)
                            .checkTournamentComplete(restState(Lost).bid(p1, Win1))
                            .resolveCategories();

                    console.checkTournament(Open, restState(Play))
                            .scoreSet(p2, 11, p3, 5)
                            .checkResult(p2, p3)
                            .checkTournamentComplete(restState(Lost).bid(p2, Win1).bid(p3, Win2));
                });
    }


    @Test
    public void consoleTourRankByResult() {
        final TournamentScenario scenario = begin().name("consoleTourRankByResult")
                .rules(RULES_G8Q1_S1A2G11)
                .category(c1, p1, p2, p3, p4);
        isf.create(scenario)
                .run(c -> {
                    final ImperativeSimulator console = c.beginTournament()
                            .createConsoleTournament()
                            .scoreSet(p1, 11, p3, 4)
                            .scoreSet(p2, 11, p4, 6)
                            .scoreSet(p1, 11, p2, 3)
                            .scoreSet(p3, 11, p4, 7)
                            .scoreSet(p1, 11, p4, 5)
                            .scoreSet(p2, 11, p3, 5)
                            .checkTournamentComplete(restState(Lost).bid(p1, Win1))
                            .resolveCategories();

                    console.checkTournament(Open, restState(Play).bid(p3, Wait))
                            .scoreSet(p2, 11, p4, 5)
                            .scoreSet(p2, 11, p3, 5)
                            .scoreSet(p3, 11, p4, 7)
                            .checkResult(p2, p3)
                            .checkTournamentComplete(restState(Lost).bid(p2, Win1).bid(p3, Win2));
                });
    }
}
