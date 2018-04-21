package org.dan.ping.pong.simulate;

import static org.dan.ping.pong.app.bid.BidState.Lost;
import static org.dan.ping.pong.app.bid.BidState.Win1;
import static org.dan.ping.pong.app.group.GroupRulesConst.G8Q2_M;
import static org.dan.ping.pong.app.match.MatchRulesConst.S3;
import static org.dan.ping.pong.app.tournament.TournamentRulesConst.RULES_G2Q1_S1A2G11;
import static org.dan.ping.pong.app.tournament.TournamentRulesConst.RULES_G2Q1_S1A2G11_NP;
import static org.dan.ping.pong.app.tournament.TournamentRulesConst.RULES_G3Q1_S1A2G11_NP;
import static org.dan.ping.pong.app.tournament.TournamentRulesConst.RULES_G3Q2_S1A2G11;
import static org.dan.ping.pong.app.tournament.TournamentRulesConst.RULES_G8Q1_S1A2G11;
import static org.dan.ping.pong.app.tournament.TournamentRulesConst.RULES_G8Q1_S1A2G11_NP;
import static org.dan.ping.pong.app.tournament.TournamentRulesConst.RULES_G8Q1_S3A2G11;
import static org.dan.ping.pong.app.tournament.TournamentRulesConst.RULES_G8Q2_S1A2G11;
import static org.dan.ping.pong.app.tournament.TournamentRulesConst.RULES_JP_S1A2G11;
import static org.dan.ping.pong.app.tournament.TournamentRulesConst.RULES_JP_S1A2G11_3P;
import static org.dan.ping.pong.app.tournament.TournamentRulesConst.RULES_LC_S1A2G11_NP;
import static org.dan.ping.pong.app.tournament.TournamentResource.TOURNAMENT_RULES;
import static org.dan.ping.pong.mock.simulator.AutoResolution.RANDOM;
import static org.dan.ping.pong.mock.simulator.FixedSetGenerator.game;
import static org.dan.ping.pong.mock.simulator.Hook.BeforeScore;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p10;
import static org.dan.ping.pong.mock.simulator.Player.p11;
import static org.dan.ping.pong.mock.simulator.Player.p12;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.Player.p4;
import static org.dan.ping.pong.mock.simulator.Player.p5;
import static org.dan.ping.pong.mock.simulator.Player.p6;
import static org.dan.ping.pong.mock.simulator.Player.p7;
import static org.dan.ping.pong.mock.simulator.Player.p8;
import static org.dan.ping.pong.mock.simulator.Player.p9;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.dan.ping.pong.mock.simulator.TournamentScenario.begin;
import static org.dan.ping.pong.mock.simulator.imerative.BidStatesDesc.restState;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.tournament.TidIdentifiedRules;
import org.dan.ping.pong.mock.simulator.EnlistMode;
import org.dan.ping.pong.mock.simulator.Simulator;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.dan.ping.pong.mock.simulator.imerative.ImperativeSimulator;
import org.dan.ping.pong.mock.simulator.imerative.ImperativeSimulatorFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Optional;

import javax.inject.Inject;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ImperativeSimulatorFactory.class, SimulationCtx.class})
public class OneCategorySim {
    @Inject
    private Simulator simulator;

    @Test
    @SneakyThrows
    public void pauseBeforeSecondScore() {
        simulator.simulate(
                begin()
                        .rules(RULES_G8Q1_S3A2G11)
                        .ignoreUnexpectedGames()
                        .autoResolution(RANDOM)
                        .name("pauseBeforeSecondScore")
                        .category(c1, p1, p2, p3, p4)
                        .win(p1, p2)
                        .pause(p1, p3, BeforeScore)
                        .win(p1, p3));
    }

    @Test
    @SneakyThrows
    public void justEnlist() {
        simulator.simulate(
                begin()
                        .rules(RULES_G8Q1_S3A2G11)
                        .ignoreUnexpectedGames()
                        .name("justEnlist")
                        .category(c1, p1, p2, p3, p4)
                        .doNotBegin()
                        .presence(EnlistMode.Enlist, p1, p2, p3, p4));
    }


    @Test
    @SneakyThrows
    public void schedule3ConcurrentGames() {
        simulator.simulate(
                begin()
                        .tables(3)
                        .rules(RULES_G8Q1_S3A2G11)
                        .ignoreUnexpectedGames()
                        .name("sched 3 con games")
                        .category(c1, p1, p2, p3, p4, p5, p6));
    }

    @Test
    @SneakyThrows
    public void justBeginTournamentOf2() {
        simulator.simulate(
                begin()
                        .rules(RULES_G8Q1_S3A2G11)
                        .ignoreUnexpectedGames()
                        .name("justBeginTournamentOf2")
                        .category(c1, p1, p2));
    }

    @Test
    @SneakyThrows
    public void justBeginTournamentOf2To1() {
        simulator.simulate(
                begin()
                        .rules(RULES_G8Q1_S1A2G11)
                        .ignoreUnexpectedGames()
                        .name("justBeginTourOf2To1")
                        .category(c1, p1, p2));
    }

    @Test
    @SneakyThrows
    public void justBeginTournamentOf3() {
        simulator.simulate(
                begin()
                        .rules(RULES_G8Q1_S3A2G11)
                        .ignoreUnexpectedGames()
                        .name("justBeginTournamentOf3")
                        .category(c1, p1, p2, p3));
    }

    @Test
    public void quits2Of3() {
        simulator.simulate(begin()
                .name("quits2Of3")
                .rules(RULES_G8Q2_S1A2G11)
                .category(c1, p1, p2, p3)
                .win(p1, p2)
                .win(p1, p3)
                .win(p2, p3)
                .quitsGroup(p1, p2)
                .win(p1, p2)
                .champions(c1, p1, p2));
    }

    @Test(expected = AssertionError.class)
    public void quits2Of2() {
        simulator.simulate(begin()
                .ignoreUnexpectedGames()
                .rules(RULES_G8Q2_S1A2G11)
                .name("quits2Of2")
                .category(c1, p1, p2));
    }

    @Test
    public void tournamentOf2() {
        simulator.simulate(begin()
                .ignoreUnexpectedGames()
                .rules(RULES_G8Q1_S1A2G11)
                .name("tournamentOf2")
                .category(c1, p1, p2));
    }

    @Test
    public void tournamentOf2CountOnlySets() {
        simulator.simulate(begin()
                .ignoreUnexpectedGames()
                .tables(0)
                .rules(RULES_G8Q1_S1A2G11_NP.withMatch(S3))
                .name("tOf2CountOnlySets")
                .category(c1, p1, p2));
    }

    @Test
    public void almostComplete() {
        simulator.simulate(begin()
                .name("almostComplete")
                .rules(RULES_G2Q1_S1A2G11)
                .ignoreUnexpectedGames()
                .category(c1, p1, p2, p3, p4)
                .win(p1, p2)
                .win(p3, p4)
                .quitsGroup(p1, p3)
                .win(p1, p3)
                .pause(p1, p3, BeforeScore)
                .champions(c1, p1, p3));
    }

    @Test
    public void scoreLastSet() {
        simulator.simulate(begin()
                .name("scoreLastSet")
                .rules(RULES_G8Q1_S3A2G11)
                .ignoreUnexpectedGames()
                .category(c1, p1, p2)
                .custom(game(p1, p2, 11, 0, 11, 1, 0, 0, 11, 2))
                .quitsGroup(p1)
                .champions(c1, p1));
    }

    @Inject
    private ImperativeSimulatorFactory isf;

    @Test
    public void twoGroupsBy3AlmostComplete() {
        isf.create(begin().name("2GroupsBy3AlmostCompleteS1")
                .rules(RULES_G3Q2_S1A2G11)
                .category(c1, p1, p2, p3, p4, p5, p6))
                .run(c -> c.beginTournament()
                        .scoreSet(p1, 11, p3, 3)
                        .scoreSet(p4, 11, p6, 6)

                        .scoreSet(p1, 11, p2, 2)
                        .scoreSet(p4, 11, p5, 1)

                        .scoreSet(p2, 11, p3, 5)
                        .scoreSet(p5, 11, p6, 7)

                        .scoreSet(p1, 11, p5, 5)
                        .scoreSet(p4, 11, p2, 2));
    }

    @Test
    public void twoGroupsBy2AlmostComplete() {
        isf.create(begin().name("2GroupsBy2AlmostCompleteS1")
                .rules(RULES_G2Q1_S1A2G11)
                .category(c1, p1, p2, p3, p4))
                .run(c -> c.beginTournament()
                        .scoreSet(p1, 11, p2, 3)
                        .scoreSet(p3, 11, p4, 7));
    }

    @Test
    public void aGroupsOf4AlmostCompleteT2() {
        isf.create(begin().name("aGroupsOf4AlmostCompleteT2")
                .tables(2)
                .rules(RULES_G8Q1_S3A2G11)
                .category(c1, p1, p2, p3, p4))
                .run(c -> c.beginTournament()
                        .scoreSet3(p1, 11, p3, 3)
                        .scoreSet3(p4, 11, p2, 4)
                        .scoreSet(p1, 11, p2, 2)
                        .expelPlayer(p4));
                        //.scoreSet3(p4, 11, p3, 3)

                        //.scoreSet3(p1, 11, p4, 5)
                        //.scoreSet3(p2, 11, p3, 7));
    }

    @Test
    public void diceGroupOf3() {
        isf.create(begin().name("diceGroupOf3")
                .rules(RULES_G8Q1_S1A2G11.withGroup(Optional.of(G8Q2_M)))
                .category(c1, p1, p2, p3))
                .run(c -> c.beginTournament()
                        .scoreSet(p1, 11, p3, 0)
                        .scoreSet(p2, 11, p1, 0)
                        .scoreSet(p3, 11, p2, 0));
    }

    @Test
    public void completeJustPlayOff8() {
        isf.create(begin().name("completeJustPlayOff8")
                .rules(RULES_JP_S1A2G11)
                // 1, 8, ;     5, 4,  3, 6, 7, 2
                .category(c1, p1, p2, p3, p4, p5, p6, p7, p8))
                .run(c -> c.beginTournament()
                        .scoreSet(p1, 11, p8, 8)
                        .scoreSet(p4, 11, p5, 5)

                        .scoreSet(p3, 11, p6, 6)
                        .scoreSet(p2, 11, p7, 7)

                        .scoreSet(p1, 11, p4, 4)
                        .scoreSet(p2, 11, p3, 3)

                        .scoreSet(p1, 11, p2, 2));
    }

    @Test
    public void completeGroupAndPlayOff() {
        isf.create(begin().name("completeGroupAndPlayOff")
                .rules(RULES_G3Q2_S1A2G11)
                .category(c1, p1, p2, p3, p4, p5, p6))
                .run(c -> c.beginTournament()
                        .scoreSet(p3, 11, p1, 2)
                        .scoreSet(p6, 11, p4, 2)

                        .scoreSet(p1, 11, p2, 1)
                        .scoreSet(p4, 11, p5, 0)

                        .scoreSet(p2, 11, p3, 4)
                        .scoreSet(p5, 11, p6, 4)

                        .reloadMatchMap()
                        .scoreSet(p3, 8, p4, 11)
                        .scoreSet(p1, 11, p6, 5)
                        .expelPlayer(p1)
                        .rescoreMatch(p2, p3, 11, 3));
    }

    @Test
    public void jpOf6With3rdPlaceBeginning() {
        isf.create(begin().name("jpOf6With3rdStart")
                .rules(RULES_JP_S1A2G11_3P)
                .category(c1, p1, p2, p3, p4, p5, p6))
                .run(ImperativeSimulator::beginTournament);
    }

    @Test
    public void setup2Group() {
        isf.create(begin().name("setup2Group")
                .rules(RULES_G3Q2_S1A2G11.withPlace(Optional.empty()))
                .category(c1, p1, p2, p3, p4, p5))
                .run(c -> c.beginTournament()
                        .scoreSet(p1, 11, p3, 3));
    }

    @Test
    public void playOffDraft() {
        isf.create(begin().name("playOffDraft")
                .rules(RULES_G2Q1_S1A2G11_NP)
                .category(c1, p1, p2, p3, p4))
                .run(c -> c.beginTournament()
                        .scoreSet(p1, 11, p2, 3));
    }

    @Test
    public void withConsoleTournament() {
        final TournamentScenario scenario = begin().name("withConsoleTournament")
                .rules(RULES_G8Q1_S1A2G11_NP)
                .category(c1, p1, p2, p3);
        isf.create(scenario)
                .run(c -> c.beginTournament()
                        .createConsoleTournament()
                        .scoreSet(p1, 11, p2, 3)
                        .scoreSet(p2, 11, p3, 5)
                        .scoreSet(p1, 11, p3, 4)
                        .checkTournamentComplete(restState(Lost).bid(p1, Win1))
                        .resolveCategories());
    }

    @Test
    public void withLayeredConsoleTournament() {
        final TournamentScenario scenario = begin().name("withLayeredConsoleTour")
                .rules(RULES_G8Q1_S1A2G11_NP)
                .category(c1, p1, p2, p3);
        isf.create(scenario)
                .run(c -> {
                    final ImperativeSimulator console = c.beginTournament()
                            .scoreSet(p1, 11, p2, 3)
                            .scoreSet(p2, 11, p3, 5)
                            .scoreSet(p1, 11, p3, 4)
                            .checkTournamentComplete(restState(Lost).bid(p1, Win1))
                            .createConsoleTournament()
                            .resolveCategories();

                    console.getMyRest().voidPost(TOURNAMENT_RULES, scenario.getTestAdmin(),
                            TidIdentifiedRules.builder()
                                    .tid(console.getScenario().getTid())
                                    .rules(RULES_LC_S1A2G11_NP)
                                    .build());
                });
    }

    @Test
    public void withLayeredConsoleTournament2Layers() {
        final TournamentScenario scenario = begin().name("with2LayeredConsoleTour")
                .rules(RULES_G3Q1_S1A2G11_NP)
                .category(c1, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12);
        isf.create(scenario)
                .run(c -> {
                    final ImperativeSimulator console = c.beginTournament()
                            .scoreSet(p1, 11, p2, 3)
                            .scoreSet(p2, 11, p3, 5)
                            .scoreSet(p1, 11, p3, 4)

                            .scoreSet(p4, 11, p5, 3)
                            .scoreSet(p5, 11, p6, 5)
                            .scoreSet(p4, 11, p6, 4)

                            .scoreSet(p7, 11, p8, 3)
                            .scoreSet(p8, 11, p9, 5)
                            .scoreSet(p7, 11, p9, 4)

                            .scoreSet(p10, 11, p11, 3)
                            .scoreSet(p11, 11, p12, 5)
                            .scoreSet(p10, 11, p12, 4)

                            .createConsoleTournament()
                            .resolveCategories();

                    console.getMyRest().voidPost(TOURNAMENT_RULES, scenario.getTestAdmin(),
                            TidIdentifiedRules.builder()
                                    .tid(console.getScenario().getTid())
                                    .rules(RULES_LC_S1A2G11_NP)
                                    .build());
                });
    }
}
