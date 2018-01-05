package org.dan.ping.pong.app.tournament;

import static org.dan.ping.pong.app.bid.BidState.Expl;
import static org.dan.ping.pong.app.bid.BidState.Lost;
import static org.dan.ping.pong.app.bid.BidState.Win1;
import static org.dan.ping.pong.app.bid.BidState.Win2;
import static org.dan.ping.pong.app.group.GroupResource.CID;
import static org.dan.ping.pong.app.group.GroupResource.GROUP_POPULATIONS;
import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_G2Q1_S1A2G11;
import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_G8Q1_S1A2G11;
import static org.dan.ping.pong.app.match.MatchResource.BID_PENDING_MATCHES;
import static org.dan.ping.pong.app.tournament.TournamentResource.TOURNAMENT_ENLIST_OFFLINE;
import static org.dan.ping.pong.mock.simulator.Hook.AfterMatch;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.Player.p4;
import static org.dan.ping.pong.mock.simulator.Player.p5;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.junit.Assert.assertThat;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.app.bid.Uid;
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
    public void enlistToNewGroup() {
        final TournamentScenario scenario = TournamentScenario
                .begin()
                .name("enlistToNewGroup")
                .rules(RULES_G8Q1_S1A2G11)
                .category(c1, p1, p2);
        final ImperativeSimulator simulator = isf.create(scenario);
        simulator.run(c -> {
            c.beginTournament();
            final int cid = scenario.getCategoryDbId().get(c1);
            final Uid uidP3 = enlistParticipant(scenario, cid, Optional.empty(), "p3");
            final GroupPopulations populations = myRest()
                    .get(GROUP_POPULATIONS + scenario.getTid().getTid() + CID + cid,
                            GroupPopulations.class);
            final int newGid = populations.getLinks().stream()
                    .map(GroupLink::getGid)
                    .max(Integer::compare).get();
            final Uid uidP4 = enlistParticipant(scenario, cid, Optional.of(newGid), "p4");

            scenario.addPlayer(uidP3, p3);
            scenario.addPlayer(uidP4, p4);

            c.scoreSet(p1, 11, p2, 3)
                    .reloadMatchMap()
                    .scoreSet(p3, 11, p4, 7)
                    .scoreSet(p1, 11, p3, 4)
                    .checkResult(p1, p3, p4, p2)
                    .checkTournamentComplete(BidStatesDesc.restState(Lost).bid(p3, Win2).bid(p1, Win1));
        });
    }

    @Test
    public void enlistToGroupWithParticipantPlayedAllGames() {
        final TournamentScenario scenario = TournamentScenario
                .begin()
                .name("enlistToGrpWithFinishedParticipant")
                .rules(RULES_G8Q1_S1A2G11.withPlayOff(Optional.empty()).withPlace(Optional.empty()))
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

            final Uid joinedUid = enlistParticipant(scenario, cid, populations, "p4");
            scenario.addPlayer(joinedUid, p4);
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

    private Uid enlistParticipant(TournamentScenario scenario, int cid, GroupPopulations populations, String p5) {
        return enlistParticipant(scenario, cid, Optional.of(populations.getLinks().get(0).getGid()), p5);
    }

    private Uid enlistParticipant(TournamentScenario scenario, int cid, Optional<Integer> gid, String p5) {
        return myRest().post(TOURNAMENT_ENLIST_OFFLINE, scenario,
                EnlistOffline.builder()
                        .groupId(gid)
                        .tid(scenario.getTid())
                        .cid(cid)
                        .bidState(BidState.Wait)
                        .name(p5)
                        .build()).readEntity(Uid.class);
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

            final Uid joinedUid = myRest().post(TOURNAMENT_ENLIST_OFFLINE, scenario,
                    EnlistOffline.builder()
                            .groupId(Optional.of(populations.getLinks().get(1).getGid()))
                            .tid(scenario.getTid())
                            .cid(cid)
                            .bidState(BidState.Wait)
                            .name("p5")
                            .build()).readEntity(Uid.class);
            scenario.addPlayer(joinedUid, p5);
            c.scoreSet(p1, 11, p2, 2)
                    .scoreSet(p3, 11, p4, 7)
                    .scoreSet(p3, 11, p5, 8)
                    .scoreSet(p4, 11, p5, 9)
                    //
                    .scoreSet(p1, 11, p3, 4)
                    .checkResult(p1, p3, p4, p5, p2)
                    .checkTournamentComplete(BidStatesDesc
                            .restState(Lost).bid(p3, Win2).bid(p1, Win1));
        });
    }

    @Test
    public void enlistToGroupWithResignedParticipant() {
        TournamentScenario scenario = TournamentScenario.begin().name("enlistToGroupWithExpelledPart")
                .rules(RULES_G8Q1_S1A2G11)
                .category(c1, p1, p2, p3);
        isf.create(scenario)
                .run(c -> {
                    c.beginTournament().scoreSet(p1, 11, p3, 0).expelPlayer(p1);
                    final int cid = scenario.getCategoryDbId().get(c1);

                    final GroupPopulations populations = myRest()
                            .get(GROUP_POPULATIONS + scenario.getTid().getTid() + CID + cid,
                                    GroupPopulations.class);

                    final Uid joinedUid = enlistParticipant(scenario, cid, populations, "p4");
                    scenario.addPlayer(joinedUid, p4);
                    c.scoreSet(p2, 11, p3, 3)
                            .scoreSet(p2, 11, p4, 4)
                            .scoreSet(p3, 11, p4, 4)
                            .checkResult(p2, p3, p4, p1)
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
                            final Uid uid = enlistParticipant(s, cid, populations, "p4");
                            s.addPlayer(uid, p4);
                            assertThat(myRest().get(BID_PENDING_MATCHES + s.getTid().getTid() + "/" + uid.getId(),
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
                            final Uid uid = enlistParticipant(s, cid, populations, "p4");
                            s.addPlayer(uid, p4);
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
