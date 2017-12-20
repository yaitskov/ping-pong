package org.dan.ping.pong.app.castinglots;

import static org.dan.ping.pong.app.bid.BidState.Win1;
import static org.dan.ping.pong.app.castinglots.MatchScheduleInGroupJerseyTest.S1A2G11;
import static org.dan.ping.pong.app.match.MatchJerseyTest.GLOBAL;
import static org.dan.ping.pong.app.playoff.PlayOffRule.Losing2;
import static org.dan.ping.pong.mock.DaoEntityGeneratorWithAdmin.INCREASE_SIGNUP_CASTING;
import static org.dan.ping.pong.mock.simulator.FixedSetGenerator.game;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p17;
import static org.dan.ping.pong.mock.simulator.Player.p18;
import static org.dan.ping.pong.mock.simulator.Player.p19;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p20;
import static org.dan.ping.pong.mock.simulator.Player.p21;
import static org.dan.ping.pong.mock.simulator.Player.p22;
import static org.dan.ping.pong.mock.simulator.Player.p23;
import static org.dan.ping.pong.mock.simulator.Player.p24;
import static org.dan.ping.pong.mock.simulator.Player.p25;
import static org.dan.ping.pong.mock.simulator.Player.p26;
import static org.dan.ping.pong.mock.simulator.Player.p27;
import static org.dan.ping.pong.mock.simulator.Player.p28;
import static org.dan.ping.pong.mock.simulator.Player.p29;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.Player.p30;
import static org.dan.ping.pong.mock.simulator.Player.p31;
import static org.dan.ping.pong.mock.simulator.Player.p32;
import static org.dan.ping.pong.mock.simulator.Player.p4;
import static org.dan.ping.pong.mock.simulator.Player.p5;
import static org.dan.ping.pong.mock.simulator.Player.p6;
import static org.dan.ping.pong.mock.simulator.Player.p7;
import static org.dan.ping.pong.mock.simulator.Player.p8;
import static org.dan.ping.pong.mock.simulator.Player.p9;
import static org.dan.ping.pong.mock.simulator.Player.p10;
import static org.dan.ping.pong.mock.simulator.Player.p11;
import static org.dan.ping.pong.mock.simulator.Player.p12;
import static org.dan.ping.pong.mock.simulator.Player.p13;
import static org.dan.ping.pong.mock.simulator.Player.p14;
import static org.dan.ping.pong.mock.simulator.Player.p15;
import static org.dan.ping.pong.mock.simulator.Player.p16;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.app.tournament.JerseyWithSimulator;
import org.dan.ping.pong.app.tournament.TournamentRules;
import org.dan.ping.pong.mock.simulator.Simulator;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.dan.ping.pong.mock.simulator.imerative.BidStatesDesc;
import org.dan.ping.pong.mock.simulator.imerative.ImperativeSimulatorFactory;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;

import javax.inject.Inject;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class JustPlayOff2DefeatsJerseyTest extends AbstractSpringJerseyTest {
    private static final TournamentRules RULES_PO_S1A2G11 = TournamentRules
            .builder()
            .match(S1A2G11)
            .group(Optional.empty())
            .casting(INCREASE_SIGNUP_CASTING)
            .playOff(Optional.of(Losing2))
            .place(Optional.of(GLOBAL))
            .build();

    @Inject
    private Simulator simulator;

    @Inject
    private ImperativeSimulatorFactory isf;

    @Test
    public void noAutoLoosers2() {
        isf.create(TournamentScenario.begin()
                .name("noAutoLoosers2")
                .rules(RULES_PO_S1A2G11)
                .category(c1, p1, p2))
                .run(c -> c.beginTournament()
                        .scoreSet(p1, 11, p2, 2)
                        .reloadMatchMap()
                        .scoreSet(p2, 11, p1, 1)
                        .checkResult(p2, p1)
                        .checkTournamentComplete(BidStatesDesc
                                .restState(BidState.Win2)
                                .bid(p2, Win1))
                        .checkPlayOffLevels(2, 2));
    }

    @Test
    public void oneAutoLooser4() {
        final TournamentScenario scenario = TournamentScenario.begin()
                .rules(RULES_PO_S1A2G11)
                .category(c1, p1, p2, p3)
                .quitsGroup(p1, p2, p3)
                .win(p2, p3)
                .win(p1, p2)
                .win(p3, p2)
                .win(p1, p3)
                .champions(c1, p1, p3, p2)
                .name("oneAutoLooser4");

        simulator.simulate(scenario);
    }

    @Test
    public void noAutoLoosers4() {
        final TournamentScenario scenario = TournamentScenario.begin()
                .rules(RULES_PO_S1A2G11)
                .category(c1, p1, p2, p3, p4)
                .quitsGroup(p1, p2, p3, p4)
                .win(p1, p4)
                .win(p2, p3)
                .win(p1, p2)
                .win(p3, p4)
                .win(p3, p2)
                .win(p1, p3)
                .champions(c1, p1, p3, p2)
                .name("noAutoLoosers4");

        simulator.simulate(scenario);
    }

    @Test
    public void noAutoLoosers8() {
        final TournamentScenario scenario = TournamentScenario.begin()
                .rules(RULES_PO_S1A2G11)
                .category(c1, p1, p2, p3, p4, p5, p6, p7, p8)
                .quitsGroup(p1, p2, p3, p4, p5, p6, p7, p8)
                // first round
                .win(p1, p8)
                .win(p4, p5)
                .win(p3, p6)
                .win(p2, p7)
                // second round
                .win(p1, p4)
                .win(p2, p3)
                // third round
                .win(p1, p2)
                // losers
                .custom(game(p5, p8, 11, 9))
                .custom(game(p6, p7, 11, 0))
                //
                .custom(game(p3, p5, 11, 9))
                .custom(game(p4, p6, 11, 9))
                .win(p3, p4)
                // loser get third place
                .win(p3, p2)
                // final
                .win(p3, p1)
                .champions(c1, p3, p1, p2)
                .name("noAutoLoosers8");

        simulator.simulate(scenario);

        isf.resume(scenario)
                .run(c -> c.checkResult(p3, p1, p2, p4, p6, p5, p8, p7)
                        .checkPlayOffLevels(6, 6, 5, 4, 3, 3, 2, 2));

    }

    @Test
    public void noAutoLoosers16() {
        final TournamentScenario scenario = TournamentScenario.begin()
                .rules(RULES_PO_S1A2G11)
                .category(c1, p1, p2, p3, p4, p5, p6, p7, p8,
                        p9, p10, p11, p12, p13, p14, p15, p16)
                .quitsGroup(p1, p1, p2, p3, p4, p5, p6, p7, p8,
                        p9, p10, p11, p12, p13, p14, p15, p16)
                // first round
                .win(p1, p16)
                .win(p8, p9)
                .win(p5, p12)
                .win(p4, p13)
                //
                .win(p3, p14)
                .win(p6, p11)
                .win(p7, p10)
                .win(p2, p15)
                // second round
                .win(p1, p8)
                .win(p4, p5)
                .win(p3, p6)
                .win(p2, p7)
                // third round
                .win(p1, p4)
                .win(p2, p3)
                // forth round
                .win(p1, p2)
                // losers
                .win(p9, p16)
                .win(p12, p13)
                .win(p11, p14)
                .win(p10, p15)
                //
                .win(p7, p9)
                .win(p6, p12)
                .win(p5, p11)
                .win(p8, p10)
                //
                .win(p6, p7)
                .win(p5, p8)
                //
                .win(p3, p6)
                .win(p4, p5)
                //
                .win(p3, p4)
                //
                .win(p2, p3)
                // final
                .win(p2, p1)
                .champions(c1, p2, p1, p3)
                .name("noAutoLoosers16");

        simulator.simulate(scenario);
    }

    @Test
    public void noAutoLoosers32() {
        final TournamentScenario scenario = TournamentScenario.begin()
                .rules(RULES_PO_S1A2G11)
                .category(c1, p1, p2, p3, p4, p5, p6, p7, p8,
                        p9, p10, p11, p12, p13, p14, p15, p16,
                        p17, p18, p19, p20, p21, p22, p23, p24,
                        p25, p26, p27, p28, p29, p30, p31, p32)
                .quitsGroup(p1, p1, p2, p3, p4, p5, p6, p7, p8,
                        p9, p10, p11, p12, p13, p14, p15, p16,
                        p17, p18, p19, p20, p21, p22, p23, p24,
                        p25, p26, p27, p28, p29, p30, p31, p32)
                // first round
                .win(p1, p32)
                .win(p16, p17)
                .win(p9, p24)
                .win(p8, p25)
                .win(p5, p28)
                .win(p12, p21)
                .win(p13, p20)
                .win(p4, p29)
                //
                .win(p3, p30)
                .win(p14, p19)
                .win(p11, p22)
                .win(p6, p27)
                .win(p7, p26)
                .win(p10, p23)
                .win(p15, p18)
                .win(p2, p31)
                // second round
                .win(p1, p16)
                .win(p8, p9)
                .win(p5, p12)
                .win(p4, p13)
                .win(p3, p14)
                .win(p6, p11)
                .win(p7, p10)
                .win(p2, p15)
                // third round
                .win(p1, p8)
                .win(p4, p5)
                .win(p3, p6)
                .win(p2, p7)
                // forth round
                .win(p1, p4)
                .win(p2, p3)
                // fifth round
                .win(p1, p2)
                // losers
                .win(p17, p32)
                .win(p24, p25)
                .win(p21, p28)
                .win(p20, p29)
                .win(p19, p30)
                .win(p22, p27)
                .win(p23, p26)
                .win(p18, p31)
                //
                .win(p15, p17)
                .win(p10, p24)
                .win(p11, p21)
                .win(p14, p20)
                .win(p13, p19)
                .win(p12, p22)
                .win(p9, p23)
                .win(p16, p18)
                //
                .win(p10, p15)
                .win(p11, p14)
                .win(p12, p13)
                .win(p9, p16)
                //
                .win(p7, p10)
                .win(p6, p11)
                .win(p5, p12)
                .win(p8, p9)
                //
                .win(p6, p7)
                .win(p5, p8)
                //
                .win(p3, p6)
                .win(p4, p5)
                //
                .win(p3, p4)
                //
                .win(p2, p3)
                // final
                .win(p1, p2)
                .champions(c1, p1, p2, p3)
                .name("noAutoLoosers32");

        simulator.simulate(scenario);
    }
}
