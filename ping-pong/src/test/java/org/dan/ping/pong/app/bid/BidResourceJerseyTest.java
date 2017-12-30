package org.dan.ping.pong.app.bid;

import static java.util.Arrays.asList;
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
import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_G3Q2_S1A2G11;
import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_G8Q1_S3A2G11;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.Player.p4;
import static org.dan.ping.pong.mock.simulator.Player.p5;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.dan.ping.pong.mock.simulator.TournamentScenario.begin;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.group.GroupInfo;
import org.dan.ping.pong.app.group.GroupWithMembers;
import org.dan.ping.pong.app.group.TournamentGroups;
import org.dan.ping.pong.app.tournament.JerseyWithSimulator;
import org.dan.ping.pong.app.user.UserLink;
import org.dan.ping.pong.mock.simulator.Simulator;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.dan.ping.pong.mock.simulator.imerative.BidStatesDesc;
import org.dan.ping.pong.mock.simulator.imerative.ImperativeSimulator;
import org.dan.ping.pong.mock.simulator.imerative.ImperativeSimulatorFactory;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
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

        final List<UserLink> result = myRest().post(
                FIND_BIDS_BY_STATE,
                scenario.getTestAdmin(),
                FindByState.builder().tid(scenario.getTid())
                        .states(asList(Wait, Play)).build())
                .readEntity(
                        new GenericType<List<UserLink>>() {});

        assertThat(result.stream().map(UserLink::getUid)
                .collect(toSet()), is(scenario.getUidPlayer().keySet()));
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
                        + scenario.player2Uid(p1).getId(), BidProfile.class);

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

        final Uid uidP3 = scenario.player2Uid(p3);

        final int tid = scenario.getTid().getTid();
        final TournamentGroups groupsInfo = myRest()
                .get(GROUP_LIST + tid, TournamentGroups.class);

        final int sourceGid = groupsInfo.getGroups().stream()
                .map(groupInfo -> myRest().get(MEMBERS + tid + "/" + groupInfo.getGid(),
                        GroupWithMembers.class))
                .filter(members -> members.getMembers().stream()
                        .anyMatch(member -> member.getUid().equals(uidP3)))
                .map(GroupWithMembers::getGid)
                .findAny()
                .get();

        myRest().voidPost(BID_CHANGE_GROUP, scenario.getTestAdmin(),
                ChangeGroupReq.builder()
                        .tid(scenario.getTid())
                        .uid(uidP3)
                        .expectedGid(sourceGid)
                        .targetGid(groupsInfo.getGroups().stream()
                                .filter(gi -> gi.getGid() != sourceGid)
                                .map(GroupInfo::getGid)
                                .findAny()
                                .get())
                        .build());

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
}
