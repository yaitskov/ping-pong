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
import static org.dan.ping.pong.app.bid.BidState.Win3;
import static org.dan.ping.pong.app.castinglots.rank.ParticipantRankingPolicy.MasterOutcome;
import static org.dan.ping.pong.app.match.MatchResource.BID_PENDING_MATCHES;
import static org.dan.ping.pong.app.match.MatchResource.OPEN_MATCHES_FOR_JUDGE;
import static org.dan.ping.pong.app.playoff.ConsoleLayersPolicy.IndependentLayers;
import static org.dan.ping.pong.app.playoff.PlayOffGuests.JustLosers;
import static org.dan.ping.pong.app.playoff.PlayOffRule.Losing1;
import static org.dan.ping.pong.app.playoff.PlayOffRule.Losing2;
import static org.dan.ping.pong.app.tournament.TournamentResource.GET_TOURNAMENT_RULES;
import static org.dan.ping.pong.app.tournament.TournamentRulesConst.RULES_G2Q1_S1A2G11_NP;
import static org.dan.ping.pong.app.tournament.TournamentRulesConst.RULES_G3Q1_S1A2G11_NP;
import static org.dan.ping.pong.app.tournament.TournamentRulesConst.RULES_G8Q1_S1A2G11;
import static org.dan.ping.pong.app.tournament.TournamentRulesConst.RULES_G8Q1_S1A2G11_NP;
import static org.dan.ping.pong.app.tournament.TournamentRulesConst.RULES_G_S1A2G11_NP;
import static org.dan.ping.pong.app.tournament.TournamentRulesConst.RULES_JP_S1A2G11_NP;
import static org.dan.ping.pong.app.tournament.TournamentRulesConst.RULES_LC_S1A2G11_NP;
import static org.dan.ping.pong.app.tournament.TournamentState.Draft;
import static org.dan.ping.pong.app.tournament.TournamentState.Open;
import static org.dan.ping.pong.app.tournament.console.TournamentRelationType.ConOff;
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
import org.dan.ping.pong.app.castinglots.rank.ParticipantRankingPolicy;
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
                    final Tid consoleTid = c.beginTournament()
                            .createConsoleGruTournament().getScenario()
                            .getConsoleTid().get();
                    assertThat(consoleTid, greaterThan(scenario.getTid()));
                    assertThat(myRest().get(
                            GET_TOURNAMENT_RULES + consoleTid.getTid(), TournamentRules.class),
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
                            .createConsoleGruTournament()
                            .resolveCategories();
                    console.checkTournament(Draft, restState(Want).bid(p2, Here).bid(p3, Here))
                            .updateRules(RULES_LC_S1A2G11_NP)
                            .beginTournament()
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

                    final ImperativeSimulator console = c.createConsoleGruTournament()
                            .resolveCategories()
                            .updateRules(RULES_LC_S1A2G11_NP);

                    c.scoreSet(p3, 11, p4, 7)
                            .scoreSet(p1, 11, p3, 4)
                            .checkTournamentComplete(restState(Lost).bid(p3, Win2).bid(p1, Win1));

                    console.checkTournament(Open, restState(Want).bid(p2, Play).bid(p4, Play))
                            .reloadMatchMap()
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
                            .createConsoleGruTournament()
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
                            .createConsoleGruTournament()
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
                            .createConsoleGruTournament()
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
                            .createConsoleGruTournament()
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
                            .createConsoleGruTournament()
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
                            .createConsoleGruTournament()
                            .scoreSet(p1, 11, p3, 4)
                            .scoreSet(p1, 11, p2, 3)
                            .scoreSet(p2, 11, p3, 5)
                            .checkTournamentComplete(restState(Lost).bid(p1, Win1))
                            .resolveCategories();
                    final int masterTid = scenario.getTid().getTid();
                    final int p2Bid = scenario.player2Bid(p2).intValue();
                    final Tid consoleTid = scenario.getConsoleTid().get();
                    assertThat(
                            myRest().get(BID_PENDING_MATCHES + masterTid  + "/" + p2Bid,
                                    MyPendingMatchList.class).getMatches(),
                            hasItem(
                                    allOf(hasProperty("tid", is(consoleTid)),
                                            hasProperty("enemy", optionalWithValue(
                                                    hasProperty("bid", is(scenario.player2Bid(p3))))),
                                            hasProperty("state", is(MatchState.Game)))));

                    final int p1Bid = scenario.player2Bid(p1).intValue();
                    assertThat(
                            myRest().get(BID_PENDING_MATCHES + masterTid + "/" + p1Bid,
                                    MyPendingMatchList.class).getMatches(),
                            is(Collections.emptyList()));

                    assertThat(
                            myRest().get(OPEN_MATCHES_FOR_JUDGE + masterTid,
                                    OpenMatchForJudgeList.class),
                            hasProperty("matches", hasItem(allOf(
                                    hasProperty("tid", is(consoleTid)),
                                    hasProperty("participants", hasItems(
                                            hasProperty("bid", is(scenario.player2Bid(p2))),
                                            hasProperty("bid", is(scenario.player2Bid(p3)))))))));
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
                            .createConsoleGruTournament()
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
                            .createConsoleGruTournament()
                            .updateConsoleRules(RULES_LC_S1A2G11_NP);

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
                            .createConsoleGruTournament()
                            .updateConsoleRules(RULES_LC_S1A2G11_NP);

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
                            .createConsoleGruTournament()
                            .updateConsoleRules(RULES_LC_S1A2G11_NP);

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
                            .createConsoleGruTournament()
                            .updateConsoleRules(RULES_LC_S1A2G11_NP);

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

    @Test
    public void consolePlayOffTour() {
        final TournamentScenario scenario = begin().name("conOffTour")
                .rules(RULES_JP_S1A2G11_NP)
                .category(c1, p1, p2, p3, p4);
        isf.create(scenario)
                .run(c -> {
                    final ImperativeSimulator console = c.beginTournament()
                            .createConsoleTournament(ConOff)
                            .scoreSet(p1, 11, p4, 5)
                            .scoreSet(p2, 11, p3, 5)
                            .scoreSet(p1, 11, p2, 3)
                            .checkTournamentComplete(restState(Lost).bid(p2, Win2).bid(p1, Win1))
                            .resolveCategories();

                    console.checkTournament(Open, restState(Play))
                            .scoreSet(p3, 11, p4, 7)
                            .checkResult(p3, p4)
                            .checkTournamentComplete(restState(Lost).bid(p3, Win1).bid(p4, Win2));
                });
    }

    @Test
    public void layeredForGroup2Defeats() {
        final TournamentScenario scenario = begin().name("lrdForGroup2Def")
                .rules(RULES_G2Q1_S1A2G11_NP)
                .category(c1, p1, p2, p3, p4, p5, p6, p7, p8);
        isf.create(scenario)
                .run(c -> {
                    c.beginTournament()
                            .createConsoleGruTournament()
                            .updateConsoleRules(RULES_LC_S1A2G11_NP.withPlayOff(
                                    Optional.of(Losing2
                                            .withLayerPolicy(
                                                    Optional.of(IndependentLayers))
                                            .withConsoleParticipants(
                                                    Optional.of(JustLosers)))));

                    final ImperativeSimulator console = c
                            .scoreSet(p1, 11, p2, 3)
                            .scoreSet(p3, 11, p4, 7)
                            .scoreSet(p5, 11, p6, 5)
                            .scoreSet(p7, 11, p8, 8)
                            //
                            .scoreSet(p1, 11, p7, 4)
                            .scoreSet(p3, 11, p5, 5)
                            //
                            .scoreSet(p1, 11, p3, 6)
                            .checkResult(p1, p3, p5, p7, p8, p4, p6, p2)
                            .checkTournamentComplete(restState(Lost).bid(p3, Win2).bid(p1, Win1))
                            .resolveCategories();

                    console.checkTournament(Open, restState(Play))
                            .scoreSet(p2, 11, p8, 6)
                            .scoreSet(p6, 11, p4, 8)
                            // semifinal
                            .scoreSet(p2, 11, p6, 8)
                            //
                            .scoreSet(p4, 11, p8, 4)
                            // A
                            .reloadMatchMap()
                            .scoreSet(p4, 11, p6, 3)
                            .reloadMatchMap()
                            .scoreSet(p2, 11, p4, 6)
                            .checkResult(p2, p4, p6, p8)
                            .checkTournamentComplete(restState(Lost)
                                    .bid(p6, Win3)
                                    .bid(p4, Win2)
                                    .bid(p2, Win1));
                });
    }

    @Test
    public void consoleForGroupWithGroupAndPlayOff() {
        final TournamentScenario scenario = begin().name("conForGrpWithGrpAndPlayOff")
                .rules(RULES_G3Q1_S1A2G11_NP)
                .category(c1, p1, p2, p3, p4, p5);
        isf.create(scenario)
                .run(master -> {
                    master.beginTournament()
                            .createConsoleGruTournament()
                            .updateConsoleRules(RULES_G2Q1_S1A2G11_NP
                                    .withCasting(RULES_G2Q1_S1A2G11_NP
                                            .getCasting().withPolicy(MasterOutcome))
                                    .withPlayOff(
                                            Optional.of(Losing1
                                                    .withConsoleParticipants(
                                                            Optional.of(JustLosers)))));

                    final ImperativeSimulator console = master // master groups
                            .scoreSet(p1, 11, p2, 3)
                            .scoreSet(p1, 11, p3, 7)
                            .scoreSet(p2, 11, p3, 7)

                            .scoreSet(p4, 11, p5, 5)
                            .resolveCategories();

                    console.checkTournament(Open, restState(Play))
                            //.scoreSet(p2, 11, ?, 6)
                            .scoreSet(p3, 11, p5, 8)
                            // final
                            .scoreSet(p2, 11, p3, 8)
                            .checkResult(p2, p3, p5)
                            .checkTournamentComplete(restState(Lost)
                                    .bid(p3, Win2).bid(p2, Win1));

                    master  // master play off
                            .scoreSet(p1, 11, p4, 4)
                            .checkResult(p1, p4, p2, p3, p5)
                            .checkTournamentComplete(restState(Lost).bid(p4, Win2).bid(p1, Win1));
                });
    }

    // console for play off where all gets into 1 group and no play off
    // console for play off with play off up to 2 defeats



    // merged layered console for play off with losers
    // merged layered console for play off without semifinal looser
    // merged layered console for play off with semifinal looser (master tournament has 3rd place match)
    // merged layered console for play off with w3
    // merged layered console for play off with w2
    // merged layered console for play off with w1

    // merged layered console for group with JL
    // merged layered console for group with w3
    // merged layered console for group with w2
    // merged layered console for group with w1
}
