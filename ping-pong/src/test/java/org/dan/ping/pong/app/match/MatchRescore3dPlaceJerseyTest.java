package org.dan.ping.pong.app.match;

import static org.dan.ping.pong.app.bid.BidState.Expl;
import static org.dan.ping.pong.app.bid.BidState.Lost;
import static org.dan.ping.pong.app.bid.BidState.Play;
import static org.dan.ping.pong.app.bid.BidState.Quit;
import static org.dan.ping.pong.app.bid.BidState.Wait;
import static org.dan.ping.pong.app.bid.BidState.Win1;
import static org.dan.ping.pong.app.bid.BidState.Win2;
import static org.dan.ping.pong.app.bid.BidState.Win3;
import static org.dan.ping.pong.app.tournament.TournamentRulesConst.RULES_JP_S3A2G11_3P;
import static org.dan.ping.pong.app.tournament.TournamentRulesConst.RULES_JP_S3A2G11_NP_3P;
import static org.dan.ping.pong.app.match.MatchState.Draft;
import static org.dan.ping.pong.app.match.MatchState.Game;
import static org.dan.ping.pong.app.match.MatchState.Over;
import static org.dan.ping.pong.app.match.MatchState.Place;
import static org.dan.ping.pong.app.tournament.TournamentState.Open;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.Player.p4;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.dan.ping.pong.mock.simulator.TournamentScenario.begin;
import static org.dan.ping.pong.mock.simulator.imerative.BidStatesDesc.restState;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.tournament.JerseyWithSimulator;
import org.dan.ping.pong.app.tournament.TournamentRules;
import org.dan.ping.pong.mock.simulator.imerative.ImperativeSimulatorFactory;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class MatchRescore3dPlaceJerseyTest extends AbstractSpringJerseyTest {
    @Inject
    private ImperativeSimulatorFactory isf;

    private void rescoreJustPlayOff4ResetComplete3p(TournamentRules rules, String name) {
        isf.create(begin().name(name)
                .rules(rules)
                .category(c1, p1, p2, p3, p4))
                .run(c -> c.beginTournament()
                        .scoreSet3(p1, 11, p4, 1)
                        .scoreSet3(p2, 11, p3, 8)
                        .scoreSet3(p3, 11, p4, 5)
                        .scoreSet3(p1, 11, p2, 1)
                        .rescoreMatch(p3, p4, 2, 11, 3, 11, 4, 11)
                        .checkMatchStatus(p3, p4, Over)
                        .checkResult(p1, p2, p4, p3)
                        .checkTournamentComplete(restState(Lost)
                                .bid(p4, Win3).bid(p2, Win2).bid(p1, Win1)));
    }

    @Test
    public void rescoreJustPlayOff4ResetComplete3p() {
        rescoreJustPlayOff4ResetComplete3p(RULES_JP_S3A2G11_3P, "JustPlayOff4RstFwdCmplt3p");
    }

    @Test
    public void rescoreJustPlayOff4NpResetComplete3p() {
        rescoreJustPlayOff4ResetComplete3p(RULES_JP_S3A2G11_NP_3P, "JustPlayOff4NPRstFwdCmplt3p");
    }

    private void rescoreJustPlayOff4ResetForwardComplete(TournamentRules rules, String name) {
        isf.create(begin().name(name)
                .rules(rules)
                .category(c1, p1, p2, p3, p4))
                .run(c -> c.beginTournament()
                        .scoreSet3(p1, 11, p4, 1)
                        .scoreSet3(p2, 11, p3, 8)
                        .scoreSet3(p3, 11, p4, 5)
                        .scoreSet3(p1, 11, p2, 1)
                        .rescoreMatch(p1, p4, 2, 11, 3, 11, 4, 11)
                        .checkMatchStatus(p1, p4, Over)
                        .checkMatchStatus(p3, p1, Game)
                        .scoreSet3(p3, 11, p1, 9)
                        .checkMatchStatus(p2, p4, Game)
                        .scoreSet3(p4, 11, p2, 6)
                        .checkResult(p4, p2, p3, p1)
                        .checkTournamentComplete(restState(Lost)
                                .bid(p3, Win3).bid(p2, Win2).bid(p4, Win1)));
    }

    @Test
    public void rescoreJustPlayOff4ResetForwardComplete() {
        rescoreJustPlayOff4ResetForwardComplete(RULES_JP_S3A2G11_3P, "JustPlayOff4ResetFwdCmplt3p");
    }

    @Test
    public void rescoreJustPlayOff4NpResetForwardComplete() {
        rescoreJustPlayOff4ResetForwardComplete(RULES_JP_S3A2G11_NP_3P, "JustPlayOff4NPRstFwdCmplt3p");
    }

    private void rescoreJustPlayOff4KeepForwardComplete(TournamentRules rules, String name) {
        isf.create(begin().name(name)
                .rules(rules)
                .category(c1, p1, p2, p3, p4))
                .run(c -> c.beginTournament()
                        .scoreSet3(p1, 11, p4, 1)
                        .scoreSet3(p2, 11, p3, 4)
                        .scoreSet3(p3, 11, p4, 2)
                        .scoreSet3(p1, 11, p2, 1)
                        .rescoreMatch(p4, p1, 9, 11, 9, 11, 9, 11)
                        .checkMatchStatus(p3, p4, Over)
                        .checkMatchStatus(p1, p2, Over)
                        .checkMatchStatus(p1, p4, Over)
                        .checkResult(p1, p2, p3, p4)
                        .checkTournamentComplete(restState(Lost)
                                .bid(p3, Win3).bid(p2, Win2).bid(p1, Win1)));
    }

    @Test
    public void rescoreJustPlayOff4KeepForwardComplete() {
        rescoreJustPlayOff4KeepForwardComplete(RULES_JP_S3A2G11_3P, "JustPlayOff4KeepForwardComplete");
    }

    @Test
    public void rescoreJustPlayOff4NpKeepForwardComplete() {
        rescoreJustPlayOff4KeepForwardComplete(RULES_JP_S3A2G11_NP_3P, "JustPlayOff4NPKeepForwardComplete");
    }

    private void rescoreJustPlayOff4KeepForwardInComplete(TournamentRules rules, String name) {
        isf.create(begin().name(name)
                .rules(rules)
                .category(c1, p1, p2, p3, p4))
                .run(c -> c.beginTournament()
                        .scoreSet3(p1, 11, p4, 1)
                        .scoreSet3(p2, 11, p3, 4)
                        .scoreSet(p3, 11, p4, 2)
                        .scoreSet(p1, 11, p2, 1)
                        .rescoreMatch(p4, p1, 9, 11, 9, 11, 9, 11)
                        .checkMatchStatus(p3, p4, Game)
                        .checkMatchStatus(p1, p2, Game)
                        .checkMatchStatus(p1, p4, Over)
                        .scoreSetLast2(p3, 11, p4, 2)
                        .scoreSetLast2(p1, 11, p2, 1)
                        .checkResult(p1, p2, p3, p4)
                        .checkTournamentComplete(restState(Lost)
                                .bid(p3, Win3).bid(p2, Win2).bid(p1, Win1)));
    }

    @Test
    public void rescoreJustPlayOff4KeepForwardInComplete() {
        rescoreJustPlayOff4KeepForwardComplete(RULES_JP_S3A2G11_3P, "JustPlayOff4KeepFwdInCmplt");
    }

    @Test
    public void rescoreJustPlayOff4NpKeepForwardInComplete() {
        rescoreJustPlayOff4KeepForwardInComplete(RULES_JP_S3A2G11_NP_3P, "JustPlayOff4NPKeepFwdInCmplt");
    }

    @Test
    public void rescoreJustPlayOff4BaseMatchOfQuitSwitch() {
        isf.create(begin().name("JustPlayOff4BaseMatchOfQuitSwitch")
                .rules(RULES_JP_S3A2G11_3P)
                .category(c1, p1, p2, p3, p4))
                .run(c -> c.beginTournament()
                        .scoreSet3(p1, 11, p4, 1)
                        .scoreSet3(p2, 11, p3, 4)
                        .scoreSet3(p3, 11, p4, 5)
                        .scoreSet(p1, 11, p2, 1)
                        .playerQuits(p1)
                        .rescoreMatch(p1, p4, 9, 11, 9, 11, 9, 11)
                        .checkMatchStatus(p1, p4, Over)
                        .checkMatchStatus(p3, p1, Game)
                        .checkTournament(Open, restState(Play).bid(p2, Wait).bid(p4, Wait))
                        .playerQuits(p1) // due rescore quit status is lost and should be enforced
                        .checkMatchStatus(p4, p2, Game)
                        .scoreSet3(p4, 11, p2, 3)
                        .checkResult(p4, p2, p3, p1)
                        .checkTournamentComplete(restState(Quit)
                                .bid(p3, Win3).bid(p2, Win2).bid(p4, Win1)));
    }

    @Test
    public void rescoreJustPlayOff4NpBaseMatchOfQuitSwitch() {
        isf.create(begin().name("JustPlayOff4NpBaseMatchOfQuitSwitch")
                .rules(RULES_JP_S3A2G11_NP_3P)
                .category(c1, p1, p2, p3, p4))
                .run(c -> c.beginTournament()
                        .scoreSet3(p1, 11, p4, 1)
                        .scoreSet3(p2, 11, p3, 4)
                        .scoreSet3(p3, 11, p4, 5)
                        .scoreSet(p1, 11, p2, 1)
                        .playerQuits(p1)
                        .rescoreMatch(p1, p4, 9, 11, 9, 11, 9, 11)
                        .checkMatchStatus(p1, p4, Over)
                        .checkMatchStatus(p4, p2, Game)
                        .checkMatchStatus(p3, p1, Game)
                        .checkTournament(Open, restState(Play))
                        .scoreSet3(p4, 11, p2, 3)
                        .playerQuits(p1) // due rescore quit status is lost and should be enforced
                        .checkResult(p4, p2, p3, p1)
                        .checkTournamentComplete(restState(Quit)
                                .bid(p3, Win3).bid(p2, Win2).bid(p4, Win1)));
    }

    @Test
    public void rescoreJustPlayOff4ResetBaseCompleteToIncomplete() {
        isf.create(begin().name("JstPlOf4RstBaseToCmpltIncmplt")
                .rules(RULES_JP_S3A2G11_3P)
                .category(c1, p1, p2, p3, p4))
                .run(c -> c.beginTournament()
                        .scoreSet3(p1, 11, p4, 1)
                        .scoreSet3(p2, 11, p3, 4)
                        .scoreSet3(p3, 11, p4, 5)
                        .scoreSet3(p1, 11, p2, 1)
                        .rescoreMatch(p1, p4, 9, 11)
                        .checkMatchStatus(p1, p4, Game)
                        .checkMatchStatus(p1, p2, Draft)
                        .checkMatchStatus(p3, p4, Draft)
                        .checkTournament(Open, restState(Play).bid(p3, Wait).bid(p2, Wait))
                        .scoreSetLast2(p1, 9, p4, 11)
                        .checkMatchStatus(p2, p1 /* =p4*/, Place)
                        .checkMatchStatus(p1, p3, Game)
                        .scoreSet3(p1, 11, p3, 3)
                        .checkMatchStatus(p2, p4, Game)
                        .checkTournament(Open, restState(Play).bid(p1, Win3).bid(p3, Lost))
                        .scoreSet3(p2, 11, p4, 3)
                        .checkTournamentComplete(restState(Lost).bid(p1, Win3).bid(p4, Win2).bid(p2, Win1)));
    }

    @Test
    public void rescoreJustPlayOff4NpResetBaseToCompleteToIncomplete() {
        isf.create(begin().name("JstPlOf4NpRstBaseToCmpltIncmplt")
                .rules(RULES_JP_S3A2G11_NP_3P)
                .category(c1, p1, p2, p3, p4))
                .run(c -> c.beginTournament()
                        .scoreSet3(p1, 11, p4, 1)
                        .scoreSet3(p2, 11, p3, 4)
                        .scoreSet3(p3, 11, p4, 5)
                        .scoreSet3(p1, 11, p2, 1)
                        .rescoreMatch(p1, p4, 9, 11)
                        .checkMatchStatus(p1, p4, Game)
                        .checkMatchStatus(p1, p2, Draft)
                        .checkMatchStatus(p3, p4, Draft)
                        .checkTournament(Open, restState(Play).bid(p3, Wait).bid(p2, Wait))
                        .scoreSetLast2(p1, 9, p4, 11)
                        .checkMatchStatus(p2, p4, Game)
                        .checkMatchStatus(p1, p3, Game)
                        .scoreSet3(p1, 11, p3, 3)
                        .checkMatchStatus(p2, p4, Game)
                        .checkTournament(Open, restState(Play).bid(p1, Win3).bid(p3, Lost))
                        .scoreSet3(p2, 11, p4, 3)
                        .checkTournamentComplete(restState(Lost).bid(p1, Win3).bid(p4, Win2).bid(p2, Win1)));
    }

    private void rescoreJustPlayOff4ChangeBaseCompleteWhenFinalExpl(TournamentRules rules, String name) {
        isf.create(begin().name(name)
                .rules(rules)
                .category(c1, p1, p2, p3, p4))
                .run(c -> c.beginTournament()
                        .scoreSet3(p1, 11, p4, 1)
                        .scoreSet3(p2, 11, p3, 4)
                        .scoreSet3(p4, 11, p3, 5)
                        .scoreSet(p1, 11, p2, 1)
                        .expelPlayer(p2)
                        .rescoreMatch3(p1, p4, 9, 11)
                        .checkMatchStatus(p1, p4, Over)
                        .checkMatchStatus(p1 /*as p4*/, p2, Over)
                        .scoreSet3(p3, 11, p1, 2)
                        .checkTournamentComplete(restState(Lost)
                                .bid(p3, Win3).bid(p2, Expl).bid(p4, Win1)));
    }

    @Test
    public void rescoreJustPlayOff4ChangeBaseCompleteWhenFinalExpl() {
        rescoreJustPlayOff4ChangeBaseCompleteWhenFinalExpl(
                RULES_JP_S3A2G11_3P, "JstPlOf4ChngBaseCmpltFinalExpl");
    }

    @Test
    public void rescoreJustPlayOff4NpChangeBaseCompleteWhenFinalExpl() {
        rescoreJustPlayOff4ChangeBaseCompleteWhenFinalExpl(
                RULES_JP_S3A2G11_NP_3P, "JstPlOf4NpChngBaseCmpltFinalExpl");
    }

    private void rescoreJustPlayOff4ChangeBaseCompleteWhenBothExpl(TournamentRules rules, String name) {
        isf.create(begin().name(name)
                .tables(2)
                .rules(rules)
                .category(c1, p1, p2, p3, p4))
                .run(c -> c.beginTournament()
                        .scoreSet3(p1, 11, p4, 1)
                        .scoreSet3(p2, 11, p3, 4)
                        .scoreSet(p4, 11, p3, 5)
                        .scoreSet(p1, 11, p2, 1)
                        .expelPlayer(p1)
                        .expelPlayer(p4)
                        .rescoreMatch3(p1, p4, 9, 11)
                        .checkMatchStatus(p1, p4, Over)
                        .checkMatchStatus(p1 /*as p4*/, p2, Over)
                        .checkResult(p2, p4, p3, p1)
                        .checkTournamentComplete(restState(Expl)
                                .bid(p2, Win1).bid(p3, Win3)));
    }

    @Test
    public void rescoreJustPlayOff4ChangeBaseCompleteWhenBothExpl() {
        rescoreJustPlayOff4ChangeBaseCompleteWhenBothExpl(
                RULES_JP_S3A2G11_3P, "JstPlOf4ChngBaseCmpltBothExpl");
    }

    @Test
    public void rescoreJustPlayOff4NpChangeBaseCompleteWhenBothExpl() {
        rescoreJustPlayOff4ChangeBaseCompleteWhenBothExpl(
                RULES_JP_S3A2G11_NP_3P, "JstPlOf4NpChngBaseCmpltBothExpl");
    }

    private void rescoreJustPlayOff4KeepBaseCompleteWhenFinalExpl(TournamentRules rules, String name) {
        isf.create(begin().name(name)
                .rules(rules)
                .category(c1, p1, p2, p3, p4))
                .run(c -> c.beginTournament()
                        .scoreSet3(p1, 11, p4, 1)
                        .scoreSet3(p2, 11, p3, 4)
                        .scoreSet3(p3, 11, p4, 5)
                        .scoreSet(p1, 11, p2, 1)
                        .expelPlayer(p2)
                        .rescoreMatch3(p1, p4, 11, 9)
                        .checkMatchStatus(p1, p4, Over)
                        .checkMatchStatus(p1, p2, Over)
                        .checkResult(p1, p2, p3, p4)
                        .checkTournamentComplete(restState(Lost)
                                .bid(p3, Win3).bid(p2, Expl).bid(p1, Win1)));
    }

    @Test
    public void rescoreJustPlayOff4KeepBaseCompleteWhenFinalExpl() {
        rescoreJustPlayOff4KeepBaseCompleteWhenFinalExpl(
                RULES_JP_S3A2G11_3P, "JstPlOf4ChngKeepCmpltFinalExpl");
    }

    @Test
    public void rescoreJustPlayOff4NpKeepBaseCompleteWhenFinalExpl() {
        rescoreJustPlayOff4KeepBaseCompleteWhenFinalExpl(
                RULES_JP_S3A2G11_NP_3P, "JstPlOf4NpKeepBaseCmpltFinalExpl");
    }

    private void rescoreJustPlayOff4KeepBaseCompleteWhenFinalExplRight(TournamentRules rules, String name) {
        isf.create(begin().name(name)
                .rules(rules)
                .category(c1, p1, p2, p3, p4))
                .run(c -> c.beginTournament()
                        .scoreSet3(p1, 11, p4, 1)
                        .scoreSet3(p2, 11, p3, 4)
                        .scoreSet3(p3, 11, p4, 5)
                        .checkMatchStatus(p1, p2, Game)
                        .expelPlayer(p2)
                        .rescoreMatch3(p1, p4, 11, 9)
                        .checkMatchStatus(p1, p4, Over)
                        .checkMatchStatus(p1, p2, Over)
                        .checkResult(p1, p2, p3, p4)
                        .checkTournamentComplete(restState(Lost)
                                .bid(p3, Win3).bid(p2, Expl).bid(p1, Win1)));
    }

    @Test
    public void rescoreJustPlayOff4KeepBaseCompleteWhenFinalExplRight() {
        rescoreJustPlayOff4KeepBaseCompleteWhenFinalExplRight(
                RULES_JP_S3A2G11_3P, "JstPlOf4ChngKeepCmpltFinalExplRight");
    }

    @Test
    public void rescoreJustPlayOff4NpKeepBaseCompleteWhenFinalExplRight() {
        rescoreJustPlayOff4KeepBaseCompleteWhenFinalExplRight(
                RULES_JP_S3A2G11_NP_3P, "JstPlOf4NpKeepBaseCmpltFnlExplRght");
    }

    private void rescoreJustPlayOff4BaseExpl(TournamentRules rules, String name) {
        isf.create(begin().name(name).tables(2)
                .rules(rules)
                .category(c1, p1, p2, p3, p4))
                .run(c -> c.beginTournament()
                        .scoreSet3(p1, 11, p4, 1)
                        .scoreSet3(p2, 11, p3, 4)
                        .scoreSet(p1, 1, p2, 11)
                        .scoreSet(p3, 1, p4, 11)
                        .checkMatchStatus(p1, p2, Game)
                        .checkMatchStatus(p3, p4, Game)
                        .expelPlayer(p1)
                        .rescoreMatch3(p1, p4, 11, 9)
                        .scoreSetLast2(p3, 1, p4, 11)
                        .checkMatchStatus(p1, p4, Over)
                        .checkMatchStatus(p1, p2, Over)
                        .checkMatchStatus(p3, p4, Over)
                        .checkResult(p2, p1, p4, p3)
                        .checkTournamentComplete(restState(Lost)
                                .bid(p4, Win3).bid(p1, Expl).bid(p2, Win1)));
    }

    @Test
    public void rescoreJustPlayOff4BaseExpl() {
        rescoreJustPlayOff4BaseExpl(
                RULES_JP_S3A2G11_3P, "JstPlOf4BaseExpl");
    }

    @Test
    public void rescoreJustPlayOff4NpBaseExpl() {
        rescoreJustPlayOff4BaseExpl(
                RULES_JP_S3A2G11_NP_3P, "JstPlOf4NpBaseExpl");
    }
}
