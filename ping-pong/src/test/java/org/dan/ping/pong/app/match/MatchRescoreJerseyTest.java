package org.dan.ping.pong.app.match;

import static java.util.Arrays.asList;
import static org.dan.ping.pong.app.bid.BidState.Expl;
import static org.dan.ping.pong.app.bid.BidState.Lost;
import static org.dan.ping.pong.app.bid.BidState.Play;
import static org.dan.ping.pong.app.bid.BidState.Quit;
import static org.dan.ping.pong.app.bid.BidState.Wait;
import static org.dan.ping.pong.app.bid.BidState.Win1;
import static org.dan.ping.pong.app.bid.BidState.Win2;
import static org.dan.ping.pong.app.match.MatchEditorService.DONT_CHECK_HASH;
import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_G_S1A2G11;
import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_G_S1A2G11_NP;
import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_G_S3A2G11;
import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_G_S3A2G11_NP;
import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_JP_S1A2G11;
import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_JP_S1A2G11_NP;
import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_JP_S3A2G11;
import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_JP_S3A2G11_NP;
import static org.dan.ping.pong.app.match.MatchState.Game;
import static org.dan.ping.pong.app.match.MatchState.Over;
import static org.dan.ping.pong.app.match.MatchState.Place;
import static org.dan.ping.pong.app.tournament.TournamentState.Open;
import static org.dan.ping.pong.mock.simulator.FixedSetGenerator.game;
import static org.dan.ping.pong.mock.simulator.HookDecision.Score;
import static org.dan.ping.pong.mock.simulator.HookDecision.Skip;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.Player.p4;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.dan.ping.pong.mock.simulator.TournamentScenario.begin;
import static org.dan.ping.pong.mock.simulator.imerative.BidStatesDesc.restState;

import com.google.common.collect.ImmutableMap;
import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.tournament.JerseyWithSimulator;
import org.dan.ping.pong.app.tournament.TournamentRules;
import org.dan.ping.pong.mock.simulator.Hook;
import org.dan.ping.pong.mock.simulator.MatchMetaInfo;
import org.dan.ping.pong.mock.simulator.PlayHook;
import org.dan.ping.pong.mock.simulator.Simulator;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.dan.ping.pong.mock.simulator.imerative.ImperativeSimulatorFactory;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class MatchRescoreJerseyTest extends AbstractSpringJerseyTest {
    @Inject
    private Simulator simulator;

    @Test
    public void rescoreEndedMatchGroup2NoPlayOff() {
        MatchMetaInfo[] mm = new MatchMetaInfo[1];
        final TournamentScenario scenario = begin()
                .name("rescoreEndedMtcG2PO2")
                .rules(RULES_G_S3A2G11)
                .category(c1, p1, p2)
                .custom(game(p1, p2, 0, 11, 1, 11, 2, 11))
                .pause(p1, p2, PlayHook.builder()
                        .type(Hook.AfterMatch)
                        .callback((s, m) -> {
                            if (mm[0] == null) {
                                mm[0] = m;
                                simulator.rescore(s,
                                        m.getOpenMatch().getMid(),
                                        ImmutableMap.of(p1, asList(11, 11, 11),
                                                p2, asList(3, 4, 5)), DONT_CHECK_HASH);
                            }
                            return Skip;
                        }).build())
                .quitsGroup(p1)
                .champions(c1, p1);

        simulator.simulate(scenario);
    }

    @Inject
    private ImperativeSimulatorFactory isf;

    @Test
    public void rescoreEndedMatchGroup2NoPlayOffImperative() {
        endedMatchGroup2NoPlayOff("rescoreEndedMtcG2PO2i", RULES_G_S3A2G11);
    }

    @Test
    public void rescoreEndedMatchGroup2NoPlayOffImperativeNp() {
        endedMatchGroup2NoPlayOff("rescoreEndedMtcG2PO2iNp", RULES_G_S3A2G11_NP);
    }

    private void endedMatchGroup2NoPlayOff(String name, TournamentRules rules) {
        isf.create(begin().name(name)
                .rules(rules)
                .category(c1, p1, p2)).run(c -> c.beginTournament()
                        .scoreSet3(p1, 2, p2, 11)
                        .checkTournamentComplete(restState(Lost).bid(p2, Win1))
                        .rescoreMatch(p1, p2, 11, 3, 11, 4, 11, 5)
                        .checkTournamentComplete(restState(Lost).bid(p1, Win1)));
    }

    @Test
    public void rescoreKeepOpenGroupMatchNoPlayOff2Matches() {
        final int[] c = new int[1];
        final TournamentScenario scenario = begin()
                .name("rescoreKeepOpenMtchGrpNPO2")
                .rules(RULES_G_S3A2G11)
                .category(c1, p1, p2)
                .custom(game(p2, p1, 11, 0, 11, 1, 2, 11))
                .pause(p1, p2, PlayHook.builder()
                        .type(Hook.BeforeScore)
                        .callback((s, m) -> {
                            if (++c[0] == 3) {
                                simulator.rescore(s,
                                        m.getOpenMatch().getMid(),
                                        ImmutableMap.of(p2, asList(0, 1, 2),
                                                p1, asList(11, 11, 11)), DONT_CHECK_HASH);
                            }
                            return Score;
                        }).build())
                .quitsGroup(p1)
                .champions(c1, p1);

        simulator.simulate(scenario);
    }

    @Test
    public void rescoreCompleteNotLastMatchNoPlayOff3Matches() {
        MatchMetaInfo[] p1p2m = new MatchMetaInfo[1];
        MatchMetaInfo[] mm = new MatchMetaInfo[1];
        final TournamentScenario scenario = begin()
                .name("rescoreCompleteNotLastGrpMtchNPO3")
                .rules(RULES_G_S3A2G11)
                .category(c1, p1, p2, p3)
                .custom(game(p1, p3, 11, 0, 11, 1, 11, 2))
                .custom(game(p2, p1, 11, 0, 11, 1, 11, 2))
                .pause(p1, p2, PlayHook.builder()
                        .type(Hook.AfterMatch)
                        .callback((s, m) -> {
                            p1p2m[0] = m;
                            return Score;
                        })
                        .build())
                .custom(game(p2, p3, 11, 0, 11, 1, 11, 2))
                .pause(p2, p3, PlayHook.builder()
                        .type(Hook.AfterScore)
                        .callback((s, m) -> {
                            if (mm[0] == null) {
                                mm[0] = m;
                                simulator.rescore(s,
                                        p1p2m[0].getOpenMatch().getMid(),
                                        ImmutableMap.of(p2, asList(0, 0, 0),
                                                p1, asList(11, 11, 11)),
                                        DONT_CHECK_HASH);
                            }
                            return Score;
                        }).build())
                .quitsGroup(p1)
                .champions(c1, p1);

        simulator.simulate(scenario);
    }

    @Test
    public void rescoreEndLastOpenMatchNoPlayOff3Matches() {
        int[] c = new int[1];
        final TournamentScenario scenario = begin()
                .name("rescoreEndLastOpenGrpMtchNPO3")
                .rules(RULES_G_S3A2G11)
                .category(c1, p1, p2, p3)
                .custom(game(p1, p3, 11, 0, 11, 1, 11, 2))
                .custom(game(p2, p1, 11, 0, 11, 1, 11, 2))
                .custom(game(p3, p2, 11, 0, 11, 1, 0, 11))
                .pause(p2, p3, PlayHook.builder()
                        .type(Hook.BeforeScore)
                        .callback((s, m) -> {
                            if (++c[0] == 3) {
                                simulator.rescore(s,
                                        m.getOpenMatch().getMid(),
                                        ImmutableMap.of(p3, asList(0, 0, 0),
                                                p2, asList(11, 11, 11)),
                                        DONT_CHECK_HASH);
                            }
                            return Score;
                        }).build())
                .quitsGroup(p2)
                .champions(c1, p2);

        simulator.simulate(scenario);
    }

    @Test
    public void rescoreLastEndedMatchGroup3NoPlayOffImperative() {
        lastEndedMatchGroupNoPlayOff("rescoreLastEndedMtcG3NPOi", RULES_G_S1A2G11);
    }

    private void lastEndedMatchGroupNoPlayOff(String name, TournamentRules rules) {
        isf.create(begin().name(name)
                .rules(rules)
                .category(c1, p1, p2, p3))
                .run(c -> c.beginTournament()
                        .scoreSet(p1, 11, p3, 0)
                        .scoreSet(p1, 11, p2, 1)
                        .scoreSet(p2, 2, p3, 11)
                        .checkResult(p1, p3, p2)
                        .checkTournamentComplete(restState(Lost).bid(p1, Win1))
                        .rescoreMatch(p1, p3, 3, 11)
                        .checkResult(p3, p1, p2)
                        .checkTournamentComplete(restState(Lost).bid(p3, Win1)));
    }

    @Test
    public void rescoreLastEndedMatchGroup3NoPlayOffImperativeNp() {
        lastEndedMatchGroupNoPlayOff("rescoreLastEndedMtcG3NPOiNp", RULES_G_S1A2G11_NP);
    }

    private void lastOpenLastEndedMatchGroupNoPlayOff(String name, TournamentRules rules) {
        isf.create(begin().name(name)
                .rules(rules)
                .category(c1, p1, p2, p3))
                .run(c -> c.beginTournament()
                        .scoreSet3(p1, 11, p3, 0)
                        .scoreSet3(p1, 11, p2, 1)
                        .scoreSet3(p2, 2, p3, 11)
                        .checkResult(p1, p3, p2)
                        .checkTournamentComplete(restState(Lost).bid(p1, Win1))
                        .rescoreMatch(p1, p3, 3, 11)
                        .checkResult(p1, p3, p2)
                        .checkTournament(Open, restState(Play).bid(p2, Wait)));
    }

    @Test
    public void rescoreOpenLastEndedMatchGroupNoPlayOff() {
        lastOpenLastEndedMatchGroupNoPlayOff("rescoreOpenLastEndedMtcG3NPOi", RULES_G_S3A2G11);
    }

    @Test
    public void rescoreOpenLastEndedMatchGroupNoPlayOffNoPlace() {
        lastOpenLastEndedMatchGroupNoPlayOff("rescoreOpenLastEndedMtcG3NPOiNp", RULES_G_S3A2G11_NP);
    }

    @Test
    public void rescoredMatchGetPlayStatusNoPlayOffNoPlace() {
        isf.create(begin().name("matchGetsPlayStatusNp")
                .rules(RULES_G_S3A2G11_NP)
                .category(c1, p1, p2, p3))
                .run(c -> c.beginTournament()
                        .scoreSet3(p1, 11, p3, 0)
                        .scoreSet3(p1, 11, p2, 1)
                        .scoreSet3(p2, 2, p3, 11)
                        .checkResult(p1, p3, p2)
                        .rescoreMatch(p1, p3, 3, 11)
                        .rescoreMatch(p1, p2, 3, 11)
                        .checkMatchStatus(p1, p2, Game)
                        .checkTournament(Open, restState(Play))
                        .scoreSetLast2(p1, 3, p2, 11)
                        .scoreSetLast2(p1, 3, p3, 11)
                        .checkResult(p3, p2, p1)
                        .checkTournamentComplete(restState(Lost).bid(p3, Win1)));
    }

    @Test
    public void rescoredMatchGetPlaceStatusNoPlayOff() {
        isf.create(begin().name("matchGetsPlaceStatus")
                .rules(RULES_G_S3A2G11)
                .category(c1, p1, p2, p3))
                .run(c -> c.beginTournament()
                        .scoreSet3(p1, 11, p3, 0)
                        .scoreSet3(p1, 11, p2, 1)
                        .scoreSet3(p2, 2, p3, 11)
                        .checkResult(p1, p3, p2)
                        .rescoreMatch(p1, p3, 3, 11)
                        .rescoreMatch(p1, p2, 3, 11)
                        .checkMatchStatus(p1, p2, Place)
                        .checkMatchStatus(p1, p3, Game)
                        .checkTournament(Open, restState(Play).bid(p2, Wait))
                        .scoreSetLast2(p1, 3, p3, 11)
                        .scoreSetLast2(p1, 3, p2, 11)
                        .checkResult(p3, p2, p1)
                        .checkTournamentComplete(restState(Lost).bid(p3, Win1)));
    }

    @Test
    public void rescoreWinnerWinMatchWithQuitParticipantNoPlayOff() {
        rescoreWinnerWinMatchWithQuiterNoPlayOff(RULES_G_S3A2G11, "winnerWinQuitParticipant2");
    }

    @Test
    public void rescoreWinnerWinMatchWithQuitParticipantNoPlayOffNP() {
        rescoreWinnerWinMatchWithQuiterNoPlayOff(RULES_G_S3A2G11_NP, "winnerWinQuitParticipant2Np");
    }

    private void rescoreWinnerWinMatchWithQuiterNoPlayOff(TournamentRules rules, String name) {
        isf.create(begin().name(name)
                .rules(rules)
                .category(c1, p1, p2))
                .run(c -> c.beginTournament()
                        .scoreSet(0, p1, 11, p2, 1)
                        .playerQuits(p1)
                        .checkMatchStatus(p1, p2, Over)
                        .checkTournamentComplete(restState(Win1).bid(p1, Quit))
                        .checkResult(p2, p1)
                        .rescoreMatch3(p1, p2, 2, 11)
                        .checkMatchStatus(p1, p2, Over)
                        .checkResult(p2, p1)
                        .checkTournamentComplete(restState(Win1).bid(p1, Quit)));
    }

    private void rescoreWinnerNotFlipMatchWithQuiterNoPlayOff(TournamentRules rules, String name) {
        isf.create(begin().name(name)
                .rules(rules)
                .category(c1, p1, p2))
                .run(c -> c.beginTournament()
                        .scoreSet(0, p1, 1, p2, 11)
                        .playerQuits(p1)
                        .checkMatchStatus(p1, p2, Over)
                        .checkTournamentComplete(restState(Win1).bid(p1, Quit))
                        .checkResult(p2, p1)
                        .rescoreMatch3(p1, p2, 11, 2)
                        .checkMatchStatus(p1, p2, Over)
                        .checkResult(p2, p1)
                        .checkTournamentComplete(restState(Win1).bid(p1, Quit)));
    }

    @Test
    public void rescoreWinnerNotFlipMatchWithQuitParticipantNoPlayOff() {
        rescoreWinnerNotFlipMatchWithQuiterNoPlayOff(RULES_G_S3A2G11, "winnerNotFlipQuitParticipant2");
    }

    @Test
    public void rescoreWinnerNotFlipMatchWithQuitParticipantNoPlayOffNP() {
        rescoreWinnerNotFlipMatchWithQuiterNoPlayOff(RULES_G_S3A2G11_NP, "winnerNotFlipQuitParticipant2Np");
    }

    private void rescoreWinnerNotFlipMatchWithExplNoPlayOff(TournamentRules rules, String name) {
        isf.create(begin().name(name)
                .rules(rules)
                .category(c1, p1, p2))
                .run(c -> c.beginTournament()
                        .scoreSet(0, p1, 1, p2, 11)
                        .expelPlayer(p1)
                        .checkMatchStatus(p1, p2, Over)
                        .checkTournamentComplete(restState(Win1).bid(p1, Expl))
                        .checkResult(p2, p1)
                        .rescoreMatch3(p1, p2, 11, 2)
                        .checkMatchStatus(p1, p2, Over)
                        .checkResult(p2, p1)
                        .checkTournamentComplete(restState(Win1).bid(p1, Expl)));
    }

    @Test
    public void rescoreWinnerNotFlipMatchWithExplParticipantNoPlayOff() {
        rescoreWinnerNotFlipMatchWithExplNoPlayOff(RULES_G_S3A2G11, "winnerNotFlipExplParticipant2");
    }

    @Test
    public void rescoreWinnerNotFlipMatchWithExplParticipantNoPlayOffNP() {
        rescoreWinnerNotFlipMatchWithQuiterNoPlayOff(RULES_G_S3A2G11_NP, "winnerNotFlipExplParticipant2Np");
    }

    @Test
    public void rescoreWinnerWinMatchWithExplParticipantNoPlayOff() {
        rescoreWinnerWinMatchWithExplNoPlayOff(RULES_G_S3A2G11, "winnerWinExplParticipant2");
    }

    @Test
    public void rescoreWinnerWinMatchWithExplParticipantNoPlayOffNP() {
        rescoreWinnerWinMatchWithExplNoPlayOff(RULES_G_S3A2G11_NP, "winnerWinExplParticipant2Np");
    }

    private void rescoreWinnerWinMatchWithExplNoPlayOff(TournamentRules rules, String name) {
        isf.create(begin().name(name)
                .rules(rules)
                .category(c1, p1, p2))
                .run(c -> c.beginTournament()
                        .scoreSet(0, p1, 11, p2, 1)
                        .expelPlayer(p1)
                        .checkMatchStatus(p1, p2, Over)
                        .checkTournamentComplete(restState(Win1).bid(p1, Expl))
                        .checkResult(p2, p1)
                        .rescoreMatch3(p1, p2, 2, 11)
                        .checkMatchStatus(p1, p2, Over)
                        .checkResult(p2, p1)
                        .checkTournamentComplete(restState(Win1).bid(p1, Expl)));
    }

    private void rescoreJustPlayOff2(TournamentRules rules, String name) {
        isf.create(begin().name(name)
                .rules(rules)
                .category(c1, p1, p2))
                .run(c -> c.beginTournament()
                        .scoreSet(p1, 11, p2, 1)
                        .checkMatchStatus(p1, p2, Over)
                        .checkTournamentComplete(restState(Win2).bid(p1, Win1))
                        .checkResult(p1, p2)
                        .rescoreMatch(p1, p2, 2, 11)
                        .checkMatchStatus(p1, p2, Over)
                        .checkResult(p2, p1)
                        .checkTournamentComplete(restState(Win2).bid(p2, Win1)));
    }

    @Test
    public void rescoreJustPlayOff2() {
        rescoreJustPlayOff2(RULES_JP_S1A2G11, "JustPlayOff2");
    }

    @Test
    public void rescoreJustPlayOff2Np() {
        rescoreJustPlayOff2(RULES_JP_S1A2G11_NP, "JustPlayOff2NP");
    }

    private void rescoreJustPlayOff2QuitBetter(TournamentRules rules, String name) {
        isf.create(begin().name(name)
                .rules(rules)
                .category(c1, p1, p2))
                .run(c -> c.beginTournament()
                        .scoreSet(p1, 11, p2, 1)
                        .playerQuits(p1)
                        .checkMatchStatus(p1, p2, Over)
                        .checkTournamentComplete(restState(Win1).bid(p1, Win2))
                        .checkResult(p2, p1)
                        .rescoreMatch3(p1, p2, 2, 11)
                        .checkMatchStatus(p1, p2, Over)
                        .checkResult(p2, p1)
                        .checkTournamentComplete(restState(Win1).bid(p1, Win2)));
    }

    @Test
    public void rescoreJustPlayOff2QuitBetter() {
        rescoreJustPlayOff2QuitBetter(RULES_JP_S3A2G11, "JustPlayOffQuitBetter2");
    }

    @Test
    public void rescoreJustPlayOff2NpQuitBetter() {
        rescoreJustPlayOff2QuitBetter(RULES_JP_S3A2G11_NP, "JustPlayOff2QuitBetterNP");
    }

    private void rescoreJustPlayOff2ExplBetter(TournamentRules rules, String name) {
        isf.create(begin().name(name)
                .rules(rules)
                .category(c1, p1, p2))
                .run(c -> c.beginTournament()
                        .scoreSet(p1, 11, p2, 1)
                        .expelPlayer(p1)
                        .checkMatchStatus(p1, p2, Over)
                        .checkTournamentComplete(restState(Win1).bid(p1, Expl))
                        .checkResult(p2, p1)
                        .rescoreMatch3(p1, p2, 2, 11)
                        .checkMatchStatus(p1, p2, Over)
                        .checkResult(p2, p1)
                        .checkTournamentComplete(restState(Win1).bid(p1, Expl)));
    }

    @Test
    public void rescoreJustPlayOff2ExplBetter() {
        rescoreJustPlayOff2ExplBetter(RULES_JP_S3A2G11, "JustPlayOffQuitBetter2");
    }

    @Test
    public void rescoreJustPlayOff2NpExplBetter() {
        rescoreJustPlayOff2ExplBetter(RULES_JP_S3A2G11_NP, "JustPlayOff2QuitBetterNP");
    }

    private void rescoreJustPlayOff2KeepIncomplete(TournamentRules rules, String name) {
        isf.create(begin().name(name)
                .rules(rules)
                .category(c1, p1, p2))
                .run(c -> c.beginTournament()
                        .scoreSet(p1, 11, p2, 1)
                        .rescoreMatch(p1, p2, 2, 11, 1, 11)
                        .checkMatchStatus(p1, p2, Game)
                        .checkTournament(Open, restState(Play).bid(p1, Play)));
    }

    @Test
    public void rescoreJustPlayOff2KeepIncomplete() {
        rescoreJustPlayOff2KeepIncomplete(RULES_JP_S3A2G11, "JustPlayOff2KeepIncomplete");
    }

    @Test
    public void rescoreJustPlayOff2NpKeepIncomplete() {
        rescoreJustPlayOff2KeepIncomplete(RULES_JP_S3A2G11_NP, "JustPlayOff2NPKeepIncomplete");
    }

    private void rescoreJustPlayOff2EndIncomplete(TournamentRules rules, String name) {
        isf.create(begin().name(name)
                .rules(rules)
                .category(c1, p1, p2))
                .run(c -> c.beginTournament()
                        .scoreSet(p1, 11, p2, 1)
                        .rescoreMatch(p1, p2, 2, 11, 1, 11, 11, 0, 11, 13)
                        .checkMatchStatus(p1, p2, Over)
                        .checkResult(p2, p1)
                        .checkTournamentComplete(restState(Win2).bid(p2, Win1)));
    }

    @Test
    public void rescoreJustPlayOff2EndIncomplete() {
        rescoreJustPlayOff2EndIncomplete(RULES_JP_S3A2G11, "JustPlayOff2EndIncomplete");
    }

    @Test
    public void rescoreJustPlayOff2NpEndIncomplete() {
        rescoreJustPlayOff2EndIncomplete(RULES_JP_S3A2G11_NP, "JustPlayOff2NPEndIncomplete");
    }

    private void rescoreJustPlayOff4ResetForwardComplete(TournamentRules rules, String name) {
        isf.create(begin().name(name)
                .rules(rules)
                .category(c1, p1, p2, p3, p4))
                .run(c -> c.beginTournament()
                        .scoreSet3(p1, 11, p4, 1)
                        .scoreSet3(p2, 11, p3, 8)
                        .scoreSet3(p1, 11, p2, 1)
                        .rescoreMatch(p1, p4, 2, 11, 3, 11, 4, 11)
                        .checkMatchStatus(p1, p4, Over)
                        .checkMatchStatus(p2, p4, Game)
                        .scoreSet3(p4, 11, p2, 6)
                        .checkResult(p4, p2, p3, p1)
                        .checkTournamentComplete(restState(Lost).bid(p2, Win2).bid(p4, Win1)));
    }

    @Test
    public void rescoreJustPlayOff4ResetForwardComplete() {
        rescoreJustPlayOff4ResetForwardComplete(RULES_JP_S3A2G11, "JustPlayOff4ResetForwardComplete");
    }

    @Test
    public void rescoreJustPlayOff4NpResetForwardComplete() {
        rescoreJustPlayOff4ResetForwardComplete(RULES_JP_S3A2G11_NP, "JustPlayOff4NPResetForwardComplete");
    }

    private void rescoreJustPlayOff4KeepForwardComplete(TournamentRules rules, String name) {
        isf.create(begin().name(name)
                .rules(rules)
                .category(c1, p1, p2, p3, p4))
                .run(c -> c.beginTournament()
                        .scoreSet3(p1, 11, p4, 1)
                        .scoreSet3(p2, 11, p3, 4)
                        .scoreSet3(p1, 11, p2, 1)
                        .rescoreMatch(p4, p1, 9, 11, 9, 11, 9, 11)
                        .checkMatchStatus(p1, p4, Over)
                        .checkResult(p1, p2, p4, p3)
                        .checkTournamentComplete(restState(Lost).bid(p2, Win2).bid(p1, Win1)));
    }

    @Test
    public void rescoreJustPlayOff4KeepForwardComplete() {
        rescoreJustPlayOff4KeepForwardComplete(RULES_JP_S3A2G11, "JustPlayOff4KeepForwardComplete");
    }

    @Test
    public void rescoreJustPlayOff4NpKeepForwardComplete() {
        rescoreJustPlayOff4KeepForwardComplete(RULES_JP_S3A2G11_NP, "JustPlayOff4NPKeepForwardComplete");
    }

    private void rescoreJustPlayOff4BaseMatchOfQuitSwitch(TournamentRules rules, String name) {
        isf.create(begin().name(name)
                .rules(rules)
                .category(c1, p1, p2, p3, p4))
                .run(c -> c.beginTournament()
                        .scoreSet3(p1, 11, p4, 1)
                        .scoreSet3(p2, 11, p3, 4)
                        .scoreSet(p1, 11, p2, 1)
                        .playerQuits(p1)
                        .rescoreMatch(p1, p4, 9, 11, 9, 11, 9, 11)
                        .checkMatchStatus(p1, p4, Over)
                        .checkMatchStatus(p4, p2, Game)
                        .checkTournament(Open, restState(Play).bid(p3, Lost)
                                // due quit => win2
                                .bid(p1, Lost))
                        .scoreSet3(p4, 11, p2, 3)
                        .checkResult(p4, p2, p1, p3)
                        .checkTournamentComplete(restState(Lost).bid(p2, Win2).bid(p4, Win1)));
    }

    @Test
    public void rescoreJustPlayOff4BaseMatchOfQuitSwitch() {
        rescoreJustPlayOff4BaseMatchOfQuitSwitch(RULES_JP_S3A2G11, "JustPlayOff4BaseMatchOfQuitSwitch");
    }

    @Test
    public void rescoreJustPlayOff4NpBaseMatchOfQuitSwitch() {
        rescoreJustPlayOff4BaseMatchOfQuitSwitch(RULES_JP_S3A2G11_NP, "JustPlayOff4NpBaseMatchOfQuitSwitch");
    }

    private void rescoreJustPlayOff4ResetBaseToCompleteToIncomplete(TournamentRules rules, String name) {
        isf.create(begin().name(name)
                .rules(rules)
                .category(c1, p1, p2, p3, p4))
                .run(c -> c.beginTournament()
                        .scoreSet3(p1, 11, p4, 1)
                        .scoreSet3(p2, 11, p3, 4)
                        .scoreSet3(p1, 11, p2, 1)
                        .rescoreMatch(p1, p4, 9, 11)
                        .checkMatchStatus(p1, p4, Game)
                        .checkTournament(Open, restState(Play).bid(p3, Lost).bid(p2, Wait))
                        .scoreSetLast2(p1, 9, p4, 11)
                        .checkMatchStatus(p2, p4, Game)
                        .checkTournament(Open, restState(Play).bid(p3, Lost).bid(p1, Lost))
                        .scoreSet3(p2, 11, p4, 3)
                        .checkTournamentComplete(restState(Lost).bid(p4, Win2).bid(p2, Win1)));
    }

    @Test
    public void rescoreJustPlayOff4ResetBaseToCompleteToIncomplete() {
        rescoreJustPlayOff4ResetBaseToCompleteToIncomplete(
                RULES_JP_S3A2G11, "JstPlOf4RstBaseToCompleteIncomplete");
    }

    @Test
    public void rescoreJustPlayOff4NpResetBaseToCompleteToIncomplete() {
        rescoreJustPlayOff4ResetBaseToCompleteToIncomplete(
                RULES_JP_S3A2G11_NP, "JstPlOf4NpRstBaseToCmpltIncmplt");
    }

    private void rescoreJustPlayOff4ResetBaseToIncomplete(TournamentRules rules, String name) {
        isf.create(begin().name(name)
                .rules(rules)
                .category(c1, p1, p2, p3, p4))
                .run(c -> c.beginTournament()
                        .scoreSet3(p1, 11, p4, 1)
                        .scoreSet3(p2, 11, p3, 4)
                        .scoreSet(p1, 11, p2, 1)
                        .rescoreMatch(p1, p4, 9, 11)
                        .checkMatchStatus(p1, p4, Game)
                        .checkTournament(Open, restState(Play).bid(p3, Lost).bid(p2, Wait))
                        .scoreSetLast2(p1, 9, p4, 11)
                        .checkMatchStatus(p2, p4, Game)
                        .checkTournament(Open, restState(Play).bid(p3, Lost).bid(p1, Lost))
                        .scoreSet3(p2, 11, p4, 3)
                        .checkTournamentComplete(restState(Lost).bid(p4, Win2).bid(p2, Win1)));
    }

    @Test
    public void rescoreJustPlayOff4ResetBaseToIncomplete() {
        rescoreJustPlayOff4ResetBaseToIncomplete(
                RULES_JP_S3A2G11, "JstPlOf4RstBaseToIncomplete");
    }

    @Test
    public void rescoreJustPlayOff4NpResetBaseToIncomplete() {
        rescoreJustPlayOff4ResetBaseToIncomplete(
                RULES_JP_S3A2G11_NP, "JstPlOf4NpRstBaseToIncomplete");
    }

    private void rescoreJustPlayOff4ChangeBaseCompleteWhenFinalExpl(TournamentRules rules, String name) {
        isf.create(begin().name(name)
                .rules(rules)
                .category(c1, p1, p2, p3, p4))
                .run(c -> c.beginTournament()
                        .scoreSet3(p1, 11, p4, 1)
                        .scoreSet3(p2, 11, p3, 4)
                        .scoreSet(p1, 11, p2, 1)
                        .expelPlayer(p2)
                        .rescoreMatch3(p1, p4, 9, 11)
                        .checkMatchStatus(p1, p4, Over)
                        .checkMatchStatus(p1 /*as p4*/, p2, Over)
                        .checkTournamentComplete(restState(Lost).bid(p2, Expl).bid(p4, Win1)));
    }

    @Test
    public void rescoreJustPlayOff4ChangeBaseCompleteWhenFinalExpl() {
        rescoreJustPlayOff4ChangeBaseCompleteWhenFinalExpl(
                RULES_JP_S3A2G11, "JstPlOf4ChngBaseCmpltFinalExpl");
    }

    @Test
    public void rescoreJustPlayOff4NpChangeBaseCompleteWhenFinalExpl() {
        rescoreJustPlayOff4ChangeBaseCompleteWhenFinalExpl(
                RULES_JP_S3A2G11_NP, "JstPlOf4NpChngBaseCmpltFinalExpl");
    }

    private void rescoreJustPlayOff4KeepBaseCompleteWhenFinalExpl(TournamentRules rules, String name) {
        isf.create(begin().name(name)
                .rules(rules)
                .category(c1, p1, p2, p3, p4))
                .run(c -> c.beginTournament()
                        .scoreSet3(p1, 11, p4, 1)
                        .scoreSet3(p2, 11, p3, 4)
                        .scoreSet(p1, 11, p2, 1)
                        .expelPlayer(p2)
                        .rescoreMatch3(p1, p4, 11, 9)
                        .checkMatchStatus(p1, p4, Over)
                        .checkMatchStatus(p1, p2, Over)
                        .checkResult(p1, p4, p3, p2)
                        .checkTournamentComplete(restState(Lost).bid(p2, Expl).bid(p1, Win1)));
    }

    @Test
    public void rescoreJustPlayOff4KeepBaseCompleteWhenFinalExpl() {
        rescoreJustPlayOff4KeepBaseCompleteWhenFinalExpl(
                RULES_JP_S3A2G11, "JstPlOf4ChngKeepCmpltFinalExpl");
    }

    @Test
    public void rescoreJustPlayOff4NpKeepBaseCompleteWhenFinalExpl() {
        rescoreJustPlayOff4KeepBaseCompleteWhenFinalExpl(
                RULES_JP_S3A2G11_NP, "JstPlOf4NpKeepBaseCmpltFinalExpl");
    }
}
