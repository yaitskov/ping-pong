package org.dan.ping.pong.app.tournament;

import static org.dan.ping.pong.app.group.GroupResource.CID;
import static org.dan.ping.pong.app.group.GroupResource.GROUP_POPULATIONS;
import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_G8Q1_S1A2G11;
import static org.dan.ping.pong.app.match.MatchResource.BID_PENDING_MATCHES;
import static org.dan.ping.pong.app.tournament.TournamentResource.TOURNAMENT_ENLIST_OFFLINE;
import static org.dan.ping.pong.mock.simulator.Hook.AfterMatch;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.Player.p4;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.junit.Assert.assertThat;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.app.group.GroupPopulations;
import org.dan.ping.pong.app.match.MyPendingMatch;
import org.dan.ping.pong.mock.simulator.HookDecision;
import org.dan.ping.pong.mock.simulator.PlayHook;
import org.dan.ping.pong.mock.simulator.Player;
import org.dan.ping.pong.mock.simulator.Simulator;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.core.GenericType;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class EnlistOfflineOpenTournamentJerseyTest extends AbstractSpringJerseyTest {
    @Inject
    private Simulator simulator;

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
                                    .get(GROUP_POPULATIONS + s.getTid() + CID + cid,
                                            GroupPopulations.class);
                            final Uid uid = myRest().post(TOURNAMENT_ENLIST_OFFLINE, s,
                                    EnlistOffline.builder()
                                            .groupId(Optional.of(populations.getLinks().get(0).getGid()))
                                            .tid(s.getTid())
                                            .cid(cid)
                                            .bidState(BidState.Wait)
                                            .name("p4")
                                            .build()).readEntity(Uid.class);
                            s.addPlayer(uid, Player.p4);
                            assertThat(myRest().get(BID_PENDING_MATCHES + s.getTid() + "/" + uid.getId(),
                                    new GenericType<List<MyPendingMatch>>() {}),
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
                                    .get(GROUP_POPULATIONS + s.getTid() + CID + cid,
                                            GroupPopulations.class);
                            final Uid uid = myRest().post(TOURNAMENT_ENLIST_OFFLINE, s,
                                    EnlistOffline.builder()
                                            .groupId(Optional.of(populations.getLinks().get(0).getGid()))
                                            .tid(s.getTid())
                                            .cid(cid)
                                            .bidState(BidState.Wait)
                                            .name("p4")
                                            .build()).readEntity(Uid.class);
                            s.addPlayer(uid, Player.p4);
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
