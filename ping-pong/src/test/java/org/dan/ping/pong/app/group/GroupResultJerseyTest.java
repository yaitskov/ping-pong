package org.dan.ping.pong.app.group;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.dan.ping.pong.app.bid.BidState.Lost;
import static org.dan.ping.pong.app.bid.BidState.Win1;
import static org.dan.ping.pong.app.group.GroupResource.GROUP_LIST;
import static org.dan.ping.pong.app.group.GroupResource.GROUP_RESULT;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.F2F;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.LostBalls;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.LostSets;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.Random;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.WonBalls;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.WonMatches;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.WonSets;
import static org.dan.ping.pong.app.match.rule.reason.DecreasingIntScalarReason.ofIntD;
import static org.dan.ping.pong.app.match.rule.reason.IncreasingIntScalarReason.ofIntI;
import static org.dan.ping.pong.app.match.rule.reason.InfoReason.notApplicableRule;
import static org.dan.ping.pong.app.tournament.TournamentResource.TOURNAMENT_ENLIST_OFFLINE;
import static org.dan.ping.pong.app.tournament.TournamentRulesConst.RULES_G2Q1_S3A2G11;
import static org.dan.ping.pong.app.tournament.TournamentRulesConst.RULES_G8Q1_S1A2G11;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.dan.ping.pong.mock.simulator.TournamentScenario.begin;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertThat;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.tournament.EnlistOffline;
import org.dan.ping.pong.app.tournament.JerseyWithSimulator;
import org.dan.ping.pong.mock.simulator.Player;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.dan.ping.pong.mock.simulator.imerative.ImperativeSimulator;
import org.dan.ping.pong.mock.simulator.imerative.ImperativeSimulatorFactory;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import java.util.Collection;
import java.util.Optional;

import javax.inject.Inject;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class GroupResultJerseyTest extends AbstractSpringJerseyTest {
    @Inject
    private ImperativeSimulatorFactory isf;

    @Test
    public void groupOf2MatchNotPlayed() {
        final TournamentScenario scenario = begin().name("groupOf2MatchNotPlayed")
                .rules(RULES_G2Q1_S3A2G11)
                .category(c1, p1, p2);
        final ImperativeSimulator simulator = isf.create(scenario);
        simulator.run(ImperativeSimulator::beginTournament);

        final int tid = scenario.getTid().getTid();

        final TournamentGroups g = groupList(tid);
        final int gid = g.getGroups().stream().findFirst().get().getGid();

        final Collection<GroupParticipantResult> l = participantsResult(tid, gid);
        assertThat(participantResultByFinish(l, 0), reasonChain1(0));
        assertThat(participantResultByFinish(l, 1), reasonChain1(1));

        assertThat(participantResultBySeed(l, 0),
                hasProperty("uid", is(scenario.player2Uid(p1))));
        assertThat(participantResultBySeed(l, 1),
                hasProperty("uid", is(scenario.player2Uid(p2))));

        simulator.run(c -> c.scoreSet(p1, 2, p2, 11));

        final Collection<GroupParticipantResult> l2 = participantsResult(tid, gid);
        assertThat(participantResultByFinish(l2, 0),
                allOf(
                        hasProperty("uid", is(scenario.player2Uid(p2))),
                        reasonChain2(1)));
        assertThat(participantResultByFinish(l2, 1),
                allOf(
                        hasProperty("uid", is(scenario.player2Uid(p1))),
                        reasonChain2(0)));

        simulator.run(c -> c.scoreSet3(1, p1, 11, p2, 5));

        final Collection<GroupParticipantResult> l3 = participantsResult(tid, gid);
        assertThat(participantResultByFinish(l3, 0),
                allOf(
                        hasProperty("uid", is(scenario.player2Uid(p1))),
                        hasProperty("state", is(Win1)),
                        reasonChain3(1)));
        assertThat(participantResultByFinish(l3, 1),
                allOf(
                        hasProperty("uid", is(scenario.player2Uid(p2))),
                        hasProperty("state", is(Lost)),
                        reasonChain3(0)));
    }

    private GroupParticipantResult participantResultByFinish(Collection<GroupParticipantResult> l, int finish) {
        return l.stream().filter(p -> p.getFinishPosition() == finish).findAny().get();
    }

    private GroupParticipantResult participantResultBySeed(Collection<GroupParticipantResult> l, int seed) {
        return l.stream().filter(p -> p.getSeedPosition() == seed).findAny().get();
    }

    private Collection<GroupParticipantResult> participantsResult(int tid, int gid) {
        return groupResult(tid, gid).getParticipants();
    }

    private Matcher<GroupParticipantResult> reasonChain1(int rndI) {
        return hasProperty("reasonChain", is(asList(
                Optional.of(ofIntD(0, WonMatches)),
                Optional.of(notApplicableRule(F2F)),
                Optional.of(ofIntD(0, WonSets)),
                Optional.of(ofIntI(0, LostSets)),
                Optional.of(notApplicableRule(F2F)),
                Optional.of(notApplicableRule(WonBalls)),
                Optional.of(notApplicableRule(LostBalls)),
                Optional.of(notApplicableRule(F2F)),
                Optional.of(ofIntI(rndI, Random)))));
    }

    private Matcher<GroupParticipantResult> reasonChain2(int i) {
        return hasProperty("reasonChain", is(asList(
                Optional.of(ofIntD(0, WonMatches)),
                Optional.of(notApplicableRule(F2F)),
                Optional.of(ofIntD(i, WonSets)))));
    }

    private Matcher<GroupParticipantResult> reasonChain3(int i) {
        return hasProperty("reasonChain", is(singletonList(Optional.of(ofIntD(i, WonMatches)))));
    }

    @Test
    public void groupOf3() {
        final TournamentScenario scenario = begin().name("groupOf3")
                .rules(RULES_G8Q1_S1A2G11)
                .category(c1, p1, p2, p3);
        final ImperativeSimulator simulator = isf.create(scenario);
        simulator.run(ImperativeSimulator::beginTournament);

        final int tid = scenario.getTid().getTid();

        simulator.run(c -> c
                        .scoreSet(p1, 11, p3, 0)
                        .scoreSet(p1, 11, p2, 1)
                        .scoreSet(p2, 2, p3, 11));


        final TournamentGroups g = groupList(tid);
        final int gid = g.getGroups().stream().findFirst().get().getGid();
        final GroupParticipants r = groupResult(tid, gid);

        assertThat(player(scenario, r, p1),
                allOf(
                        hasProperty("reasonChain", is(singletonList(Optional.of(ofIntD(2, WonMatches))))),
                        hasProperty("name", containsString("p1")),
                        hasProperty("seedPosition", is(0)),
                        hasProperty("finishPosition", is(0))));

        assertThat(player(scenario, r, p3),
                allOf(
                        hasProperty("reasonChain", is(singletonList(Optional.of(ofIntD(1, WonMatches))))),
                        hasProperty("name", containsString("p3")),
                        hasProperty("seedPosition", is(2)),
                        hasProperty("finishPosition", is(1))));
    }

    private Uid enlistParticipant(TournamentScenario scenario, int cid,
            Optional<Integer> groupId, String p5) {
        return myRest().post(TOURNAMENT_ENLIST_OFFLINE, scenario,
                EnlistOffline.builder()
                        .groupId(groupId)
                        .tid(scenario.getTid())
                        .cid(cid)
                        .bidState(BidState.Wait)
                        .name(p5)
                        .build())
                .readEntity(Uid.class);
    }

    @Test
    public void groupOf2AddNewAndInvalidateCache() {
        final TournamentScenario scenario = begin().name("groupOf2AddNew")
                .rules(RULES_G8Q1_S1A2G11)
                .category(c1, p1, p2);
        final ImperativeSimulator simulator = isf.create(scenario);
        simulator.run(ImperativeSimulator::beginTournament);

        final int tid = scenario.getTid().getTid();

        final TournamentGroups g = groupList(tid);
        final int gid = g.getGroups().stream().findFirst().get().getGid();

        enlistParticipant(scenario,
                scenario.getCategoryDbId().get(c1), Optional.of(gid), "p3");
        simulator.invalidateTournamentCache();

        final GroupParticipants r = groupResult(tid, gid);

        assertThat(r, notNullValue());
    }

    private GroupParticipants groupResult(int tid, int gid) {
        return myRest().get(GROUP_RESULT + tid + "/"
                + gid, GroupParticipants.class);
    }

    private TournamentGroups groupList(int tid) {
        return myRest().get(GROUP_LIST + tid, TournamentGroups.class);
    }

    private GroupParticipantResult player(TournamentScenario scenario,
            GroupParticipants r, Player player) {
        return r.getParticipants().stream()
                .filter(
                        p -> p.getUid().equals(scenario.player2Uid(player)))
                .findAny()
                .get();
    }
}
