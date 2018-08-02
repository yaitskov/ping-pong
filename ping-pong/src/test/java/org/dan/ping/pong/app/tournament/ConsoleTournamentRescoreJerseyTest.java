package org.dan.ping.pong.app.tournament;

import static org.dan.ping.pong.app.bid.BidState.Lost;
import static org.dan.ping.pong.app.bid.BidState.Play;
import static org.dan.ping.pong.app.bid.BidState.Win1;
import static org.dan.ping.pong.app.bid.BidState.Win2;
import static org.dan.ping.pong.app.tournament.ConsoleTournamentJerseyTest.playMasterG3Q1;
import static org.dan.ping.pong.app.tournament.TournamentRulesConst.RULES_G3Q1_S1A2G11_NP;
import static org.dan.ping.pong.app.tournament.TournamentRulesConst.RULES_LC_S1A2G11_NP;
import static org.dan.ping.pong.app.tournament.TournamentState.Open;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.Player.p4;
import static org.dan.ping.pong.mock.simulator.Player.p5;
import static org.dan.ping.pong.mock.simulator.Player.p6;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.dan.ping.pong.mock.simulator.TournamentScenario.begin;
import static org.dan.ping.pong.mock.simulator.imerative.BidStatesDesc.restState;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.dan.ping.pong.mock.simulator.imerative.ImperativeSimulator;
import org.dan.ping.pong.mock.simulator.imerative.ImperativeSimulatorFactory;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class ConsoleTournamentRescoreJerseyTest {
    @Inject
    private ImperativeSimulatorFactory isf;

    @Test
    @Ignore
    public void rescoreMasterMatchAffectingConGrpLrd() {
        final TournamentScenario scenario = begin().name("rescoreConGrpLrd")
                .rules(RULES_G3Q1_S1A2G11_NP)
                .category(c1, p1, p2, p3, p4, p5, p6);
        isf.create(scenario)
                .run(c -> {
                    c.beginTournament()
                            .createConsoleGruTournament()
                            .updateConsoleRules(RULES_LC_S1A2G11_NP);

                    final ImperativeSimulator console = playMasterG3Q1(c);

                    console.checkTournament(Open, restState(Play))
                            .reloadMatchMap()
                            .scoreSet(p2, 11, p5, 7)
                            .scoreSet(p3, 11, p6, 9);

                    c.rescoreMatch(p5, p4, 11, 9)
                            .reloadMatchMap()
                            .scoreSet(p1, 11, p5, 5)
                            .checkTournamentComplete(restState(Lost).bid(p5, Win2).bid(p1, Win1));

                    console
                            .scoreSet(p2, 11, p4, 6)
                            .checkTournamentComplete(restState(Lost)
                                    .bid(p6, Win2).bid(p3, Win1)
                                    .bid(p4, Win2).bid(p2, Win1));
                });
    }

    // rescore master match to affect group console merged layered matches
    // rescore master match to affect play off console merged layered special guess (w1, w2, w3)
    // rescore master match to affect play off console layered matches
    // rescore master match to affect play off console merged layered matches
}
