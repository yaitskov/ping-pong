package org.dan.ping.pong.simulate;

import static org.dan.ping.pong.app.castinglots.MatchScheduleInGroupJerseyTest.G8Q2_M;
import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_G2Q1_S1A2G11;
import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_G3Q2_S1A2G11;
import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_G8Q1_S1A2G11;
import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_G8Q1_S3A2G11;
import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_G8Q2_S1A2G11;
import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_JP_S1A2G11;
import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_JP_S1A2G11_3P;
import static org.dan.ping.pong.mock.simulator.AutoResolution.RANDOM;
import static org.dan.ping.pong.mock.simulator.FixedSetGenerator.game;
import static org.dan.ping.pong.mock.simulator.Hook.BeforeScore;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.Player.p4;
import static org.dan.ping.pong.mock.simulator.Player.p5;
import static org.dan.ping.pong.mock.simulator.Player.p6;
import static org.dan.ping.pong.mock.simulator.Player.p7;
import static org.dan.ping.pong.mock.simulator.Player.p8;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.dan.ping.pong.mock.simulator.TournamentScenario.begin;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.mock.simulator.EnlistMode;
import org.dan.ping.pong.mock.simulator.Simulator;
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
    public void twoGroupsBy2AlmostComplete() {
        isf.create(begin().name("2GroupsBy2AlmostCompleteS1")
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
}
