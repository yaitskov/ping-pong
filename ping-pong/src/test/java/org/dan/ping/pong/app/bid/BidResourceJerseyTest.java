package org.dan.ping.pong.app.bid;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toSet;
import static org.dan.ping.pong.app.bid.BidResource.BID_CHANGE_GROUP;
import static org.dan.ping.pong.app.bid.BidResource.FIND_BIDS_BY_STATE;
import static org.dan.ping.pong.app.bid.BidResource.PROFILE;
import static org.dan.ping.pong.app.bid.BidResource.TID_SLASH_UID;
import static org.dan.ping.pong.app.bid.BidState.Here;
import static org.dan.ping.pong.app.bid.BidState.Lost;
import static org.dan.ping.pong.app.bid.BidState.Play;
import static org.dan.ping.pong.app.bid.BidState.Wait;
import static org.dan.ping.pong.app.bid.BidState.Win1;
import static org.dan.ping.pong.app.bid.BidState.Win2;
import static org.dan.ping.pong.app.group.GroupResource.GROUP_LIST;
import static org.dan.ping.pong.app.group.GroupResource.MEMBERS;
import static org.dan.ping.pong.app.tournament.TournamentRulesConst.RULES_G2Q1_S1A2G11_NP;
import static org.dan.ping.pong.app.tournament.TournamentRulesConst.RULES_G3Q2_S1A2G11;
import static org.dan.ping.pong.app.tournament.TournamentRulesConst.RULES_G8Q1_S1A2G11;
import static org.dan.ping.pong.app.tournament.TournamentRulesConst.RULES_G8Q1_S3A2G11;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.Player.p4;
import static org.dan.ping.pong.mock.simulator.Player.p5;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.dan.ping.pong.mock.simulator.TournamentScenario.begin;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.group.GroupInfo;
import org.dan.ping.pong.app.group.GroupWithMembers;
import org.dan.ping.pong.app.group.TournamentGroups;
import org.dan.ping.pong.app.tournament.JerseyWithSimulator;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.user.UserLink;
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

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.core.GenericType;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class BidResourceJerseyTest extends AbstractSpringJerseyTest {
    @Inject
    private Simulator simulator;

    @Test
    public void findByState() {
        final TournamentScenario scenario = begin()
                .ignoreUnexpectedGames()
                .name("findByState")
                .rules(RULES_G8Q1_S3A2G11)
                .category(c1, p1, p2);

        simulator.simulate(scenario);

        final List<ParticipantLink> result = myRest().post(
                FIND_BIDS_BY_STATE,
                scenario.getTestAdmin(),
                FindByState.builder().tid(scenario.getTid())
                        .states(asList(Wait, Play)).build())
                .readEntity(
                        new GenericType<List<ParticipantLink>>() {});

        assertThat(result.stream().map(ParticipantLink::getBid)
                .collect(toSet()), is(scenario.getBidPlayer().keySet()));
    }

    @Test
    public void profile() {
        final TournamentScenario scenario = begin()
                .doNotBegin()
                .name("profile")
                .rules(RULES_G8Q1_S3A2G11)
                .category(c1, p1);

        simulator.simulate(scenario);

        final BidProfile profile = myRest()
                .get(PROFILE + scenario.getTid().getTid() + TID_SLASH_UID
                                + scenario.player2Bid(p1).intValue(),
                        scenario,
                        BidProfile.class);

        assertThat(profile, allOf(
                hasProperty("state", is(Here)),
                hasProperty("name", containsString("p1")),
                hasProperty("enlistedAt"),
                hasProperty("category",
                        hasProperty("cid", is(scenario.getCategoryDbId().get(c1))))));
    }

    @Inject
    private ImperativeSimulatorFactory isf;

    @Test
    public void changeGroupG2G3() {
        final TournamentScenario scenario = begin()
                .name("changeGroupG2G3")
                .tables(4)
                .rules(RULES_G3Q2_S1A2G11.withPlace(Optional.empty()))
                .category(c1, p1, p2, p3, p4, p5);

        final ImperativeSimulator simulator = isf.create(scenario);
        simulator.run(ImperativeSimulator::beginTournament);

        final Bid bidP3 = scenario.player2Bid(p3);

        final int tid = scenario.getTid().getTid();
        final TournamentGroups groupsInfo = myRest()
                .get(GROUP_LIST + tid, TournamentGroups.class);

        final int sourceGid = findSourceGroup(bidP3, tid, groupsInfo);

        changeGroup(scenario, bidP3, sourceGid, findTargetGroup(groupsInfo, sourceGid));

        simulator.run(c -> c
                .reloadMatchMap()
                .scoreSet(p1, 11, p2, 2)
                .scoreSet(p3, 11, p4, 4)
                .scoreSet(p4, 11, p5, 5)
                .scoreSet(p3, 11, p5, 5)
                .reloadMatchMap()
                .scoreSet(p1, 11, p4, 8)
                .scoreSet(p3, 11, p2, 3)
                .reloadMatchMap()
                .scoreSet(p1, 11, p3, 4)
                .checkResult(p1, p3, p4, p2, p5)
                .checkTournamentComplete(BidStatesDesc
                        .restState(Lost)
                        .bid(p1, Win1).bid(p3, Win2)));
    }

    private Optional<Integer> findTargetGroup(TournamentGroups groupsInfo, int sourceGid) {
        return groupsInfo.getGroups().stream()
                .filter(gi -> gi.getGid() != sourceGid)
                .map(GroupInfo::getGid)
                .findAny();
    }

    private Integer findSourceGroup(Bid bidP3, int tid, TournamentGroups groupsInfo) {
        return groupsInfo.getGroups().stream()
                .map(groupInfo -> myRest().get(MEMBERS + tid + "/" + groupInfo.getGid(),
                        GroupWithMembers.class))
                .filter(members -> members.getMembers().stream()
                        .anyMatch(member -> member.getBid().equals(bidP3)))
                .map(GroupWithMembers::getGid)
                .findAny()
                .get();
    }

    @Test
    public void changeToNewGroupG3() {
        final TournamentScenario scenario = begin()
                .name("changeToNewGroupG3")
                .rules(RULES_G8Q1_S1A2G11.withPlace(Optional.empty()))
                .category(c1, p1, p2, p3, p4);

        final ImperativeSimulator simulator = isf.create(scenario);
        simulator.run(ImperativeSimulator::beginTournament);

        final Bid bidP3 = scenario.player2Bid(p3);
        final Bid bidP4 = scenario.player2Bid(p4);

        final int tid = scenario.getTid().getTid();
        final TournamentGroups groupsInfo = myRest()
                .get(GROUP_LIST + tid, TournamentGroups.class);

        final int sourceGid = findSourceGroup(bidP3, tid, groupsInfo);

        changeGroup(scenario, bidP3, sourceGid, Optional.empty());
        changeGroup(scenario, bidP4, sourceGid,
                findTargetGroup(
                        myRest().get(GROUP_LIST + tid, TournamentGroups.class),
                        sourceGid));

        simulator.run(c -> c
                .reloadMatchMap()
                .scoreSet(p1, 11, p2, 3)
                .scoreSet(p3, 11, p4, 7)
                .reloadMatchMap()
                .scoreSet(p1, 11, p3, 4)
                .checkResult(p1, p3, p4, p2)
                .checkTournamentComplete(BidStatesDesc.restState(Lost)
                        .bid(p3, Win2).bid(p1, Win1)));
    }

    private void changeGroup(TournamentScenario scenario, Bid bidP3,
            int sourceGid, Optional<Integer> targetGid) {
        myRest().voidPost(BID_CHANGE_GROUP, scenario.getTestAdmin(),
                ChangeGroupReq.builder()
                        .tid(scenario.getTid())
                        .bid(bidP3)
                        .expectedGid(sourceGid)
                        .targetGid(targetGid)
                        .build());
    }

    @Test
    public void findByStateCombinesConsoleParticipants() {
        final TournamentScenario scenario = begin().name("findByStateWithConsole")
                .rules(RULES_G2Q1_S1A2G11_NP)
                .category(c1, p1, p2, p3, p4);
        isf.create(scenario)
                .run(c -> {
                    c.beginTournament()
                            .createConsoleTournament()
                            .scoreSet(p1, 11, p2, 4)
                            .scoreSet(p3, 11, p4, 3)
                            .resolveCategories();
                    final List<UserLink> users = myRest().post(FIND_BIDS_BY_STATE,
                            FindByState.builder()
                                    .tid(scenario.getTid())
                                    .states(asList(Play, Wait))
                                    .build())
                            .readEntity(new GenericType<List<UserLink>>() {});
                    assertThat(users,
                            hasItems(hasProperty("uid", Matchers.is(scenario.player2Bid(p1))),
                                    hasProperty("uid", Matchers.is(scenario.player2Bid(p2)))));
                });
    }

    @Test
    public void rename() {
        final TournamentScenario scenario = begin().name("renameOfflineBid")
                .rules(RULES_G2Q1_S1A2G11_NP)
                .category(c1, p1, p2);
        isf.create(scenario)
                .run(c -> {
                    c.beginTournament().renameBid(p2, "p2 renamed");
                    final List<UserLink> users = myRest().post(FIND_BIDS_BY_STATE,
                            FindByState.builder()
                                    .tid(scenario.getTid())
                                    .states(singletonList(Play))
                                    .build())
                            .readEntity(new GenericType<List<UserLink>>() {});
                    assertThat(users,
                            hasItem(
                                    allOf(
                                            hasProperty("name", Matchers.is("p2 renamed")),
                                            hasProperty("uid", Matchers.is(scenario.player2Bid(p2))))));
                });
    }
}
