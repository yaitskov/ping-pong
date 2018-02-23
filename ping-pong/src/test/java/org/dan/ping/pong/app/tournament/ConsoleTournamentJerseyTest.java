package org.dan.ping.pong.app.tournament;

import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static org.dan.ping.pong.app.bid.BidState.Expl;
import static org.dan.ping.pong.app.bid.BidState.Here;
import static org.dan.ping.pong.app.bid.BidState.Lost;
import static org.dan.ping.pong.app.bid.BidState.Play;
import static org.dan.ping.pong.app.bid.BidState.Wait;
import static org.dan.ping.pong.app.bid.BidState.Want;
import static org.dan.ping.pong.app.bid.BidState.Win1;
import static org.dan.ping.pong.app.bid.BidState.Win2;
import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_G2Q1_S1A2G11_NP;
import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_G3Q1_S1A2G11_NP;
import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_G8Q1_S1A2G11;
import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_G8Q1_S1A2G11_NP;
import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_LC_S1A2G11_NP;
import static org.dan.ping.pong.app.match.MatchResource.BID_PENDING_MATCHES;
import static org.dan.ping.pong.app.match.MatchResource.OPEN_MATCHES_FOR_JUDGE;
import static org.dan.ping.pong.app.tournament.TournamentResource.GET_TOURNAMENT_RULES;
import static org.dan.ping.pong.app.tournament.TournamentResource.TOURNAMENT_CONSOLE_CREATE;
import static org.dan.ping.pong.app.tournament.TournamentResource.TOURNAMENT_RULES;
import static org.dan.ping.pong.app.tournament.TournamentState.Draft;
import static org.dan.ping.pong.app.tournament.TournamentState.Open;
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
import static org.dan.ping.pong.mock.simulator.imerative.BidStatesDesc.restState;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.match.MatchState;
import org.dan.ping.pong.app.match.MyPendingMatchList;
import org.dan.ping.pong.app.match.OpenMatchForJudgeList;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.dan.ping.pong.mock.simulator.imerative.ImperativeSimulator;
import org.dan.ping.pong.mock.simulator.imerative.ImperativeSimulatorFactory;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import java.util.Collections;
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
    public void createConsoleTournamentForClosedTournament() {
        final TournamentScenario scenario = begin().name("consoleAfterGetClosed")
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
                    console.checkTournament(Draft, restState(Want).bid(p2, Here).bid(p3, Here));
                    myRest().voidPost(TOURNAMENT_RULES, scenario.getTestAdmin(),
                            TidIdentifiedRules.builder()
                                    .tid(c.getConsoleScenario().getTid())
                                    .rules(RULES_LC_S1A2G11_NP)
                                    .build());
                    console.beginTournament()
                            .checkTournamentComplete(restState(Win1)
                                    .bid(p2, Win1).bid(p3, Win1));
                });
    }

    @Test
    public void createConsoleTourWhen1GroupIsCompleteAndOtherNot() {
        final TournamentScenario scenario = begin().name("consoleGroupSandwich")
                .rules(RULES_G2Q1_S1A2G11_NP)
                .category(c1, p1, p2, p3, p4);
        isf.create(scenario)
                .run(c -> {
                    c.beginTournament()
                            .scoreSet(p1, 11, p2, 3);

                    final ImperativeSimulator console = c.createConsoleTournament()
                            .resolveCategories();
                    myRest().voidPost(TOURNAMENT_RULES, scenario.getTestAdmin(),
                            TidIdentifiedRules.builder()
                                    .tid(c.getConsoleScenario().getTid())
                                    .rules(RULES_LC_S1A2G11_NP)
                                    .build());

                    c.scoreSet(p3, 11, p4, 7)
                            .scoreSet(p1, 11, p3, 4)
                            .checkTournamentComplete(restState(Lost).bid(p3, Win2).bid(p1, Win1));

                    console.checkTournament(Open, restState(Want).bid(p2, Play).bid(p4, Play));
                    console.reloadMatchMap()
                            .scoreSet(p2, 11, p4, 6)
                            .checkTournamentComplete(restState(Lost)
                                    .bid(p2, Win1).bid(p4, Win2));
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

                    console.checkTournament(Open, restState(Play).bid(p2, Wait))
                            // -- p2  - winnie
                            .scoreSet(p3, 11, p4, 7)
                            .scoreSet(p2, 11, p3, 5)
                            .checkResult(p2, p3, p4)
                            .checkTournamentComplete(restState(Lost).bid(p2, Win1).bid(p3, Win2));
                });
    }

    @Test
    public void expelInMasterNp() {
        final TournamentScenario scenario = begin().name("expelInMasterNp")
                .rules(RULES_G8Q1_S1A2G11_NP)
                .category(c1, p1, p2, p3, p4);
        isf.create(scenario)
                .run(c -> {
                    final ImperativeSimulator console = c.beginTournament()
                            .createConsoleTournament()
                            .expelPlayer(p4)
                            .scoreSet(p1, 11, p3, 4)
                            .scoreSet(p1, 11, p2, 3)
                            .scoreSet(p2, 11, p3, 5)
                            .checkTournamentComplete(restState(Lost).bid(p4, Expl).bid(p1, Win1))
                            .resolveCategories();

                    console.checkTournament(Open, restState(Play).bid(p4, Expl))
                            .scoreSet(p2, 11, p3, 5)
                            .checkResult(p2, p3)
                            .checkTournamentComplete(restState(Expl).bid(p2, Win1).bid(p3, Win2));
                });
    }

    @Test
    public void expelInMasterConsole2P() {
        final TournamentScenario scenario = begin().name("expelInMasterConsole2P")
                .rules(RULES_G8Q1_S1A2G11)
                .category(c1, p1, p2, p3);
        isf.create(scenario)
                .run(c -> {
                    final ImperativeSimulator console = c.beginTournament()
                            .createConsoleTournament()
                            .expelPlayer(p3)
                            .scoreSet(p1, 11, p2, 3)
                            .checkTournamentComplete(restState(Expl).bid(p2, Lost).bid(p1, Win1))
                            .resolveCategories();

                    console.checkTournamentComplete(restState(Expl).bid(p2, Win1))
                            .checkResult(p2);
                });
    }

    @Test
    public void expelOnLastMatchInTournament() {
        final TournamentScenario scenario = begin().name("expelOnLastMatch")
                .rules(RULES_G8Q1_S1A2G11_NP)
                .category(c1, p1, p2, p3, p4);
        isf.create(scenario)
                .run(c -> {
                    final ImperativeSimulator console = c.beginTournament()
                            .createConsoleTournament()
                            .scoreSet(p1, 11, p3, 4)
                            .scoreSet(p1, 11, p2, 3)
                            .scoreSet(p2, 11, p3, 5)
                            .expelPlayer(p4)
                            .checkTournamentComplete(restState(Lost).bid(p4, Expl).bid(p1, Win1))
                            .resolveCategories();

                    console.checkTournament(Open, restState(Play).bid(p4, Expl))
                            .scoreSet(p2, 11, p3, 5)
                            .checkResult(p2, p3)
                            .checkTournamentComplete(restState(Expl).bid(p2, Win1).bid(p3, Win2));
                });
    }

    @Test
    public void bidOpenMatchIncludeConsoleTournament() {
        final TournamentScenario scenario = begin().name("bidOpenMatchIncludeConsole")
                .rules(RULES_G8Q1_S1A2G11_NP)
                .category(c1, p1, p2, p3);
        isf.create(scenario)
                .run(c -> {
                    c.beginTournament()
                            .createConsoleTournament()
                            .scoreSet(p1, 11, p3, 4)
                            .scoreSet(p1, 11, p2, 3)
                            .scoreSet(p2, 11, p3, 5)
                            .checkTournamentComplete(restState(Lost).bid(p1, Win1))
                            .resolveCategories();
                    final int masterTid = scenario.getTid().getTid();
                    final int p2Uid = scenario.player2Uid(p2).getId();
                    final Tid consoleTid = scenario.getConsoleTid().get();
                    assertThat(
                            myRest().get(BID_PENDING_MATCHES  + masterTid  + "/" + p2Uid,
                                    MyPendingMatchList.class).getMatches(),
                            hasItem(
                                    allOf(hasProperty("tid", is(consoleTid)),
                                            hasProperty("enemy", optionalWithValue(
                                                    hasProperty("uid", is(scenario.player2Uid(p3))))),
                                            hasProperty("state", is(MatchState.Game)))));

                    final int p1Uid = scenario.player2Uid(p1).getId();
                    assertThat(
                            myRest().get(BID_PENDING_MATCHES + masterTid + "/" + p1Uid,
                                    MyPendingMatchList.class).getMatches(),
                            is(Collections.emptyList()));

                    assertThat(
                            myRest().get(OPEN_MATCHES_FOR_JUDGE + masterTid, OpenMatchForJudgeList.class),
                            hasProperty("matches", hasItem(allOf(
                                    hasProperty("tid", is(consoleTid)),
                                    hasProperty("participants", hasItems(
                                            hasProperty("uid", is(scenario.player2Uid(p2))),
                                            hasProperty("uid", is(scenario.player2Uid(p3)))))))));
                });
    }

    @Test
    public void masterTour2Groups() {
        final TournamentScenario scenario = begin().name("masterTour2Groups")
                .rules(RULES_G2Q1_S1A2G11_NP)
                .category(c1, p1, p2, p3, p4);
        isf.create(scenario)
                .run(c -> {
                    final ImperativeSimulator console = c.beginTournament()
                            .createConsoleTournament()
                            .scoreSet(p1, 11, p2, 3)
                            .scoreSet(p3, 11, p4, 7)
                            .scoreSet(p1, 11, p3, 4)
                            .checkTournamentComplete(restState(Lost).bid(p3, Win2).bid(p1, Win1))
                            .resolveCategories();

                    console.checkTournament(Open, restState(Play))
                            .scoreSet(p2, 11, p4, 6)
                            .checkResult(p2, p4)
                            .checkTournamentComplete(restState(Lost).bid(p2, Win1).bid(p4, Win2));
                });
    }


    @Test
    public void layeredConsoleTournament() {
        final TournamentScenario scenario = begin().name("layeredConsoleTournament")
                .rules(RULES_G2Q1_S1A2G11_NP)
                .category(c1, p1, p2, p3, p4);
        isf.create(scenario)
                .run(c -> {
                    c.beginTournament()
                            .createConsoleTournament();

                    myRest().voidPost(TOURNAMENT_RULES, scenario.getTestAdmin(),
                            TidIdentifiedRules.builder()
                                    .tid(c.getConsoleScenario().getTid())
                                    .rules(RULES_LC_S1A2G11_NP)
                                    .build());

                    final ImperativeSimulator console = c
                            .scoreSet(p1, 11, p2, 3)
                            .scoreSet(p3, 11, p4, 7)
                            .scoreSet(p1, 11, p3, 4)
                            .checkTournamentComplete(restState(Lost).bid(p3, Win2).bid(p1, Win1))
                            .resolveCategories();

                    console.checkTournament(Open, restState(Play))
                            .scoreSet(p2, 11, p4, 6)
                            .checkResult(p2, p4)
                            .checkTournamentComplete(restState(Lost).bid(p4, Win2).bid(p2, Win1));
                });
    }

    @Test
    public void layeredConsoleTournament2Steps() {
        final TournamentScenario scenario = begin().name("layeredConsoleTour2Steps")
                .rules(RULES_G2Q1_S1A2G11_NP)
                .category(c1, p1, p2, p3, p4, p5, p6, p7, p8);
        isf.create(scenario)
                .run(c -> {
                    c.beginTournament()
                            .createConsoleTournament();

                    myRest().voidPost(TOURNAMENT_RULES, scenario.getTestAdmin(),
                            TidIdentifiedRules.builder()
                                    .tid(c.getConsoleScenario().getTid())
                                    .rules(RULES_LC_S1A2G11_NP)
                                    .build());

                    final ImperativeSimulator console = c
                            .scoreSet(p1, 11, p2, 3)
                            .scoreSet(p3, 11, p4, 7)
                            .scoreSet(p5, 11, p6, 1)
                            .scoreSet(p7, 11, p8, 1)
                            .reloadMatchMap()
                            .scoreSet(p1, 11, p7, 4)
                            .scoreSet(p3, 11, p5, 2)
                            .reloadMatchMap()
                            .scoreSet(p1, 11, p3, 6)
                            .checkTournamentComplete(restState(Lost).bid(p3, Win2).bid(p1, Win1))
                            .resolveCategories();

                    console.checkTournament(Open, restState(Play))
                            .reloadMatchMap()
                            .scoreSet(p2, 11, p8, 6)
                            .scoreSet(p4, 11, p6, 2)
                            .reloadMatchMap()
                            .scoreSet(p2, 11, p4, 6)
                            .checkTournamentComplete(restState(Lost).bid(p4, Win2).bid(p2, Win1));
                });
    }

    @Test
    public void layeredConsoleTournament2Tags() {
        final TournamentScenario scenario = begin().name("layeredConsoleTour2Tags")
                .rules(RULES_G3Q1_S1A2G11_NP)
                .category(c1, p1, p2, p3, p4, p5, p6);
        isf.create(scenario)
                .run(c -> {
                    c.beginTournament()
                            .createConsoleTournament();

                    myRest().voidPost(TOURNAMENT_RULES, scenario.getTestAdmin(),
                            TidIdentifiedRules.builder()
                                    .tid(c.getConsoleScenario().getTid())
                                    .rules(RULES_LC_S1A2G11_NP)
                                    .build());

                    final ImperativeSimulator console = c
                            .scoreSet(p1, 11, p3, 4)
                            .scoreSet(p2, 11, p3, 5)
                            .scoreSet(p1, 11, p2, 3)

                            .scoreSet(p4, 11, p6, 2)
                            .scoreSet(p5, 11, p6, 1)
                            .scoreSet(p4, 11, p5, 9)
                            .reloadMatchMap()
                            .scoreSet(p1, 11, p4, 5)
                            .checkTournamentComplete(restState(Lost).bid(p4, Win2).bid(p1, Win1))
                            .resolveCategories();

                    console.checkTournament(Open, restState(Play))
                            .reloadMatchMap()
                            .scoreSet(p2, 11, p5, 7)
                            .scoreSet(p3, 11, p6, 9)
                            .checkTournamentComplete(restState(Lost)
                                    .bid(p6, Win2).bid(p3, Win1)
                                    .bid(p5, Win2).bid(p2, Win1));
                });
    }

    @Test
    public void onePlayerOnLayer() {
        final TournamentScenario scenario = begin().name("onePlayerOnLayer")
                .rules(RULES_G3Q1_S1A2G11_NP)
                .category(c1, p1, p2, p3, p4, p5);
        isf.create(scenario)
                .run(c -> {
                    c.beginTournament()
                            .createConsoleTournament();
                    myRest().voidPost(TOURNAMENT_RULES, scenario.getTestAdmin(),
                            TidIdentifiedRules.builder()
                                    .tid(c.getConsoleScenario().getTid())
                                    .rules(RULES_LC_S1A2G11_NP)
                                    .build());

                    final ImperativeSimulator console = c
                            .scoreSet(p1, 11, p3, 4)
                            .scoreSet(p2, 11, p3, 5)
                            .scoreSet(p1, 11, p2, 3)

                            .scoreSet(p4, 11, p5, 9)
                            .reloadMatchMap()
                            .scoreSet(p1, 11, p4, 5)
                            .checkTournamentComplete(restState(Lost).bid(p4, Win2).bid(p1, Win1))
                            .resolveCategories();

                    console.checkTournament(Open, restState(Play).bid(p3, Win1))
                            .reloadMatchMap()
                            .scoreSet(p2, 11, p5, 7)
                            .checkTournamentComplete(restState(Lost)
                                    .bid(p3, Win1)
                                    .bid(p5, Win2).bid(p2, Win1));
                });
    }
}
