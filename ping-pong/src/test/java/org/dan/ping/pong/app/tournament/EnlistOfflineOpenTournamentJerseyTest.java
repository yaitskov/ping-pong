package org.dan.ping.pong.app.tournament;

import static org.dan.ping.pong.app.bid.BidState.Expl;
import static org.dan.ping.pong.app.bid.BidState.Lost;
import static org.dan.ping.pong.app.bid.BidState.Win1;
import static org.dan.ping.pong.app.bid.BidState.Win2;
import static org.dan.ping.pong.app.bid.BidState.Win3;
import static org.dan.ping.pong.app.group.GroupResource.CID;
import static org.dan.ping.pong.app.group.GroupResource.GROUP_POPULATIONS;
import static org.dan.ping.pong.app.match.MatchResource.BID_PENDING_MATCHES;
import static org.dan.ping.pong.app.playoff.PlayOffRule.L1_3P;
import static org.dan.ping.pong.app.tournament.TournamentRulesConst.RULES_G2Q1_S1A2G11;
import static org.dan.ping.pong.app.tournament.TournamentRulesConst.RULES_G8Q1_S1A2G11;
import static org.dan.ping.pong.mock.simulator.Hook.AfterMatch;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.Player.p4;
import static org.dan.ping.pong.mock.simulator.Player.p5;
import static org.dan.ping.pong.mock.simulator.Player.p6;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.junit.Assert.assertThat;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.group.GroupLink;
import org.dan.ping.pong.app.group.GroupPopulations;
import org.dan.ping.pong.app.match.MyPendingMatchList;
import org.dan.ping.pong.mock.simulator.HookDecision;
import org.dan.ping.pong.mock.simulator.PlayHook;
import org.dan.ping.pong.mock.simulator.Simulator;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.dan.ping.pong.mock.simulator.imerative.BidStatesDesc;
import org.dan.ping.pong.mock.simulator.imerative.ImperativeSimulator;
import org.dan.ping.pong.mock.simulator.imerative.ImperativeSimulatorFactory;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;

import javax.inject.Inject;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class EnlistOfflineOpenTournamentJerseyTest extends AbstractSpringJerseyTest {
    @Inject
    private Simulator simulator;

    @Inject
    private ImperativeSimulatorFactory isf;

    @Test
    public void enlistToNewGroupWithoutPlayOff() {
        final TournamentScenario scenario = TournamentScenario
                .begin()
                .name("enlistToNewGroupWithoutPlayOff")
                .rules(RULES_G2Q1_S1A2G11
                        .withPlayOff(Optional.empty())
                        .withPlace(Optional.empty()))
                .category(c1, p1, p2);
        join2PlayersIntoNewGroup(scenario);
    }

    private void join2PlayersIntoNewGroup(TournamentScenario scenario) {
        final ImperativeSimulator simulator = isf.create(scenario);
        simulator.run(c -> {
            c.beginTournament();
            final int cid = scenario.getCategoryDbId().get(c1);
            final Bid bidP3 = c.enlistParticipant(scenario, cid, Optional.empty(), "p3");
            final GroupPopulations populations = myRest()
                    .get(GROUP_POPULATIONS + scenario.getTid().getTid() + CID + cid,
                            GroupPopulations.class);
            final int newGid = populations.getLinks().stream()
                    .map(GroupLink::getGid)
                    .max(Integer::compare).get();
            final Bid bidP4 = c.enlistParticipant(scenario, cid, Optional.of(newGid), "p4");

            scenario.addPlayer(bidP3, p3);
            scenario.addPlayer(bidP4, p4);

            c.scoreSet(p1, 11, p2, 3)
                    .reloadMatchMap()
                    .scoreSet(p3, 11, p4, 7)
                    .scoreSet(p1, 11, p3, 4)
                    .checkResult(p1, p3, p4, p2)
                    .checkTournamentComplete(BidStatesDesc
                            .restState(Lost)
                            .bid(p3, Win2).bid(p1, Win1));
        });
    }

    @Test
    public void enlistToNewGroupFakeGroup() {
        final TournamentScenario scenario = TournamentScenario
                .begin()
                .name("enlistToNewGroupFakeGroup")
                .rules(RULES_G2Q1_S1A2G11
                        .withPlayOff(Optional.of(L1_3P))
                        .withPlace(Optional.empty()))
                .category(c1, p1, p2, p3, p4);
        final ImperativeSimulator simulator = isf.create(scenario);
        simulator.run(c -> {
            c.beginTournament();
            final int cid = scenario.getCategoryDbId().get(c1);
            final Bid bidP5 = c.enlistParticipant(scenario, cid, Optional.empty(), "p5");
            final GroupPopulations populations = myRest()
                    .get(GROUP_POPULATIONS + scenario.getTid().getTid() + CID + cid,
                            GroupPopulations.class);
            final int newGid = populations.getLinks().stream()
                    .map(GroupLink::getGid)
                    .max(Integer::compare).get();
            final Bid bidP6 = c.enlistParticipant(scenario, cid, Optional.of(newGid), "p6");

            scenario.addPlayer(bidP5, p5);
            scenario.addPlayer(bidP6, p6);

            c.scoreSet(p1, 11, p2, 3)
                    .scoreSet(p3, 11, p4, 7)
                    .reloadMatchMap()
                    .scoreSet(p5, 11, p6, 1)
                    .reloadMatchMap()
                    .scoreSet(p3, 11, p5, 8)
                    .reloadMatchMap()
                    .scoreSet(p1, 11, p3, 4)
                    .checkResult(p1, p3, p5, p4, p2, p6)
                    .checkTournamentComplete(BidStatesDesc.restState(Lost)
                            .bid(p5, Win3).bid(p3, Win2).bid(p1, Win1));
        });
    }

    @Test
    public void enlistToNewGroup() {
        final TournamentScenario scenario = TournamentScenario
                .begin()
                .name("enlistToNewGroup")
                .rules(RULES_G8Q1_S1A2G11)
                .category(c1, p1, p2);
        join2PlayersIntoNewGroup(scenario);
    }

    @Test
    public void enlistToGroupWithParticipantPlayedAllGames() {
        final TournamentScenario scenario = TournamentScenario
                .begin()
                .name("enlistToGrpWithFinishedParticipant")
                .rules(RULES_G8Q1_S1A2G11.withPlayOff(
                        Optional.empty()).withPlace(Optional.empty()))
                .category(c1, p1, p2, p3);
        final ImperativeSimulator simulator = isf.create(scenario);
        simulator.run(c -> {
            c.beginTournament()
                    .scoreSet(p1, 11, p3, 4)
                    .scoreSet(p2, 11, p3, 5);

            final int cid = scenario.getCategoryDbId().get(c1);

            final GroupPopulations populations = myRest()
                    .get(GROUP_POPULATIONS + scenario.getTid().getTid() + CID + cid,
                            GroupPopulations.class);

            final Bid joinedBid = c.enlistParticipant(scenario, cid, populations, "p4");
            scenario.addPlayer(joinedBid, p4);
            c.scoreSet(p1, 11, p2, 3)
                    .reloadMatchMap()
                    .scoreSet(p1, 11, p4, 5)
                    .scoreSet(p2, 11, p4, 6)
                    .scoreSet(p3, 11, p4, 7)
                    .checkResult(p1, p2, p3, p4)
                    .checkTournamentComplete(BidStatesDesc
                            .restState(Lost).bid(p1, Win1));
        });
    }

    @Test
    public void enlistToTournamentWith2GroupsAutoSelect() {
        final TournamentScenario scenario = TournamentScenario
                .begin()
                .tables(2)
                .name("enlistToTournamentWith2Groups")
                .rules(RULES_G2Q1_S1A2G11)
                .category(c1, p1, p2, p3, p4);
        final ImperativeSimulator simulator = isf.create(scenario);
        simulator.run(c -> {
            c.beginTournament();
            final int cid = scenario.getCategoryDbId().get(c1);

            final GroupPopulations populations = myRest()
                    .get(GROUP_POPULATIONS + scenario.getTid().getTid() + CID + cid,
                            GroupPopulations.class);

            final Bid joinedBid = c.enlistParticipant(scenario, cid,
                    Optional.of(populations.getLinks().get(1).getGid()), "p5");

            scenario.addPlayer(joinedBid, p5);
            c.scoreSet(p1, 11, p2, 2)
                    .scoreSet(p3, 11, p4, 7)
                    .scoreSet(p3, 11, p5, 8)
                    .scoreSet(p4, 11, p5, 9)
                    //
                    .scoreSet(p1, 11, p3, 4)
                    .checkResult(p1, p3, p4, p2, p5)
                    .checkTournamentComplete(BidStatesDesc
                            .restState(Lost).bid(p3, Win2).bid(p1, Win1));
        });
    }

    @Test
    public void enlistToGroupWithResignedParticipant() {
        TournamentScenario scenario = TournamentScenario.begin()
                .name("enlistToGroupWithExpelledPart")
                .rules(RULES_G8Q1_S1A2G11)
                .category(c1, p1, p2, p3);
        isf.create(scenario)
                .run(c -> {
                    c.beginTournament().scoreSet(p1, 11, p3, 0).expelPlayer(p1);
                    final int cid = scenario.getCategoryDbId().get(c1);

                    final GroupPopulations populations = myRest()
                            .get(GROUP_POPULATIONS + scenario.getTid().getTid() + CID + cid,
                                    GroupPopulations.class);

                    final Bid joinedBid = c.enlistParticipant(scenario, cid, populations, "p4");
                    scenario.addPlayer(joinedBid, p4);
                    c.scoreSet(p2, 11, p3, 3)
                            .scoreSet(p2, 11, p4, 4)
                            .scoreSet(p3, 11, p4, 4)
                            .checkResult(p2, p1, p3, p4)
                            .checkTournamentComplete(BidStatesDesc
                                    .restState(Lost).bid(p1, Expl).bid(p2, Win1));
                });
    }

    @Test
    public void enlistOfflineFourth() {
        final TournamentScenario scenario = TournamentScenario.begin()
                .ignoreUnexpectedGames()
                .tables(0)
                .rules(RULES_G8Q1_S1A2G11.withPlace(Optional.empty()))
                .category(c1, p1, p2, p3)
                .win(p1, p3)
                .pause(p1, p3, PlayHook.builder()
                        .type(AfterMatch)
                        .callback((s, m) -> {
                            final int cid = s.getCategoryDbId().get(c1);

                            final GroupPopulations populations = myRest()
                                    .get(GROUP_POPULATIONS + s.getTid().getTid() + CID + cid,
                                            GroupPopulations.class);
                            final Bid bid = simulator.enlistParticipant(s, cid, populations, "p4");
                            s.addPlayer(bid, p4);
                            assertThat(myRest().get(BID_PENDING_MATCHES + s.getTid().getTid()
                                            + "/" + bid.intValue(),
                                    MyPendingMatchList.class).getMatches(),
                                    Matchers.hasSize(3));
                            return HookDecision.Skip;
                        })
                        .build())
                .win(p1, p2)
                .win(p1, p4)
                .win(p4, p2)
                .win(p4, p3)
                .win(p3, p2)
                .quitsGroup(p1)
                .champions(c1, p1)
                .name("enlistOfflineFourth");

        simulator.simulate(scenario);
    }

    @Test
    public void enlistOfflineFourthFreeTable() {
        final TournamentScenario scenario = TournamentScenario.begin()
                .ignoreUnexpectedGames()
                .tables(6)
                .rules(RULES_G8Q1_S1A2G11)
                .category(c1, p1, p2, p3)
                .win(p1, p3)
                .pause(p1, p3, PlayHook.builder()
                        .type(AfterMatch)
                        .callback((s, m) -> {
                            final int cid = s.getCategoryDbId().get(c1);

                            final GroupPopulations populations = myRest()
                                    .get(GROUP_POPULATIONS + s.getTid().getTid() + CID + cid,
                                            GroupPopulations.class);
                            final Bid bid = simulator.enlistParticipant(s, cid, populations, "p4");
                            s.addPlayer(bid, p4);
                            return HookDecision.Skip;
                        })
                        .build())
                .win(p1, p2)
                .win(p1, p4)
                .win(p4, p2)
                .win(p4, p3)
                .win(p3, p2)
                .quitsGroup(p1)
                .champions(c1, p1)
                .name("enlistOffline4FreeTable");

        simulator.simulate(scenario);
    }
}
