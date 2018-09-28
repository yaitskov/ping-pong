package org.dan.ping.pong.app.group;

import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static org.dan.ping.pong.app.bid.BidState.Expl;
import static org.dan.ping.pong.app.bid.BidState.Lost;
import static org.dan.ping.pong.app.bid.BidState.Play;
import static org.dan.ping.pong.app.bid.BidState.Win1;
import static org.dan.ping.pong.app.group.GroupResource.CID;
import static org.dan.ping.pong.app.group.GroupResource.GROUP_POPULATIONS;
import static org.dan.ping.pong.app.group.GroupRulesConst.DM_ORDER_RULES_PUNKTS;
import static org.dan.ping.pong.app.match.MatchState.Over;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.F2F;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.UseDisambiguationMatches;
import static org.dan.ping.pong.app.tournament.TournamentResource.TOURNAMENT_ENLIST_OFFLINE;
import static org.dan.ping.pong.app.tournament.TournamentRulesConst.RULES_G_S1A2G11_NP;
import static org.dan.ping.pong.app.tournament.TournamentRulesConst.RULES_G_S2A2G11_NP;
import static org.dan.ping.pong.app.tournament.TournamentState.Open;
import static org.dan.ping.pong.app.user.UserResource.OFFLINE_USER_REGISTER;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.Player.p4;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.dan.ping.pong.mock.simulator.imerative.BidStatesDesc.restState;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.category.Cid;
import org.dan.ping.pong.app.match.Mid;
import org.dan.ping.pong.app.match.rule.OrderRuleName;
import org.dan.ping.pong.app.match.rule.reason.DecreasingIntScalarReason;
import org.dan.ping.pong.app.match.rule.reason.InfoReason;
import org.dan.ping.pong.app.match.rule.rules.GroupOrderRule;
import org.dan.ping.pong.app.sport.SportType;
import org.dan.ping.pong.app.tournament.EnlistOffline;
import org.dan.ping.pong.app.tournament.JerseyWithSimulator;
import org.dan.ping.pong.app.tournament.TournamentRules;
import org.dan.ping.pong.app.user.OfflineUserRegRequest;
import org.dan.ping.pong.mock.simulator.Player;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.dan.ping.pong.mock.simulator.imerative.BidStatesDesc;
import org.dan.ping.pong.mock.simulator.imerative.ImperativeSimulator;
import org.dan.ping.pong.mock.simulator.imerative.ImperativeSimulatorFactory;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class DisambiguationMatchJerseyTest extends AbstractSpringJerseyTest {
    @Inject
    private ImperativeSimulatorFactory isf;

    @Test
    public void genDisambiguationMatchesSameMatchRule() {
        final TournamentScenario scenario = ambigousScenario("genDisambiguationMatches");

        final ImperativeSimulator simulator = isf.create(scenario);
        simulator.run(c -> makeGroupAmbigous(c)
                .and(this::checkMatchesP1P2P3Over)
                .checkTournament(Open, restState(Play))
                .reloadMatchMap()
                .and(this::makeGroupUnambigous)
                .checkResult(p3, p1, p2)
                .checkTournamentComplete(restState(Lost).bid(p3, Win1))
                .and(this::validateGroupResult)
                .invalidateTournamentCache()
                .and(this::validateGroupResult));
    }

    private ImperativeSimulator validateGroupResult(ImperativeSimulator c) {
        final TournamentScenario s = c.getScenario();
        final GroupParticipants gp = c.getGroupResult(
                c.gidOf(s.getCategoryDbId().get(c1), 0));

        assertThat(gp, allOf(hasProperty("participants", contains(
                allOf(
                        hasProperty("bid", is(s.player2Bid(p1))),
                        hasProperty("seedPosition", is(0)),
                        hasProperty("finishPosition", is(1)),
                        hasProperty("dmMatches", aMapWithSize(is(2))),
                        hasProperty("originMatches", aMapWithSize(is(2))),
                        hasProperty("reasonChain", contains(
                                optionalWithValue(allOf(
                                        instanceOf(DecreasingIntScalarReason.class),
                                        hasProperty("rule", is(OrderRuleName.Punkts)),
                                        hasProperty("value", is(3)))),
                                optionalWithValue(allOf(
                                        instanceOf(InfoReason.class),
                                        hasProperty("rule", is(F2F)))),
                                optionalWithValue(allOf(
                                        instanceOf(InfoReason.class),
                                        hasProperty("rule", is(UseDisambiguationMatches)))),
                                optionalWithValue(allOf(
                                        instanceOf(DecreasingIntScalarReason.class),
                                        hasProperty("rule", is(OrderRuleName.Punkts)),
                                        hasProperty("value", is(3)))))),
                        hasProperty("name", containsString("p1"))),
                allOf(
                        hasProperty("bid", is(s.player2Bid(p2))),
                        hasProperty("seedPosition", is(1)),
                        hasProperty("finishPosition", is(2)),
                        hasProperty("dmMatches", aMapWithSize(is(2))),
                        hasProperty("originMatches", aMapWithSize(is(2))),
                        hasProperty("reasonChain", contains(
                                optionalWithValue(allOf(
                                        instanceOf(DecreasingIntScalarReason.class),
                                        hasProperty("rule", is(OrderRuleName.Punkts)),
                                        hasProperty("value", is(3)))),
                                optionalWithValue(allOf(
                                        instanceOf(InfoReason.class),
                                        hasProperty("rule", is(F2F)))),
                                optionalWithValue(allOf(
                                        instanceOf(InfoReason.class),
                                        hasProperty("rule", is(UseDisambiguationMatches)))),
                                optionalWithValue(allOf(
                                        instanceOf(DecreasingIntScalarReason.class),
                                        hasProperty("rule", is(OrderRuleName.Punkts)),
                                        hasProperty("value", is(2)))))),
                        hasProperty("name", containsString("p2"))),
                allOf(
                        hasProperty("bid", is(s.player2Bid(p3))),
                        hasProperty("seedPosition", is(2)),
                        hasProperty("finishPosition", is(0)),
                        hasProperty("dmMatches", aMapWithSize(is(2))),
                        hasProperty("originMatches", aMapWithSize(is(2))),
                        hasProperty("reasonChain", contains(
                                optionalWithValue(allOf(
                                        instanceOf(DecreasingIntScalarReason.class),
                                        hasProperty("rule", is(OrderRuleName.Punkts)),
                                        hasProperty("value", is(3)))),
                                optionalWithValue(allOf(
                                        instanceOf(InfoReason.class),
                                        hasProperty("rule", is(F2F)))),
                                optionalWithValue(allOf(
                                        instanceOf(InfoReason.class),
                                        hasProperty("rule", is(UseDisambiguationMatches)))),
                                optionalWithValue(allOf(
                                        instanceOf(DecreasingIntScalarReason.class),
                                        hasProperty("rule", is(OrderRuleName.Punkts)),
                                        hasProperty("value", is(4)))))),
                        hasProperty("name", containsString("p3"))))),
                hasProperty("quitsGroup", is(1)),
                hasProperty("sportType", is(SportType.PingPong)),
                hasProperty("tid", is(s.getTid()))));
        return c;
    }

    @Test
    public void rescoreOriginCompleteMatchKeepingAmbiguatyOpen() {
        final TournamentScenario scenario = ambigousScenario("rescoreOriginKeepOpen");
        final ImperativeSimulator simulator = isf.create(scenario);
        final Map<Set<Player>, Mid> originMatchMap = new HashMap<>();
        final Map<Set<Player>, Mid> dmMatchMap = new HashMap<>();
        simulator.run(c -> makeGroupAmbigous(c)
                .storeMatchMap(originMatchMap)
                .reloadMatchMap()
                .storeMatchMap(dmMatchMap)
                .scoreSet(p1, 11, p2, 0)
                .restoreMatchMap(originMatchMap)
                .rescoreMatch(p1, p3, 9, 11)
                .restoreMatchMap(dmMatchMap)
                .scoreSet(p3, 11, p2, 0)
                .scoreSet(p3, 11, p1, 0)
                .checkResult(p3, p1, p2)
                .checkTournamentComplete(restState(Lost).bid(p3, Win1)));
    }

    @Test
    public void rescoreOriginCompleteMatchKeepingAmbiguatyComplete() {
        final TournamentScenario scenario = ambigousScenario("rescoreOriginKeepComplete");
        final ImperativeSimulator simulator = isf.create(scenario);
        final Map<Set<Player>, Mid> originMatchMap = new HashMap<>();
        simulator.run(c -> makeGroupAmbigous(c)
                .storeMatchMap(originMatchMap)
                .reloadMatchMap()
                .and(this::makeGroupUnambigous)
                .restoreMatchMap(originMatchMap)
                .rescoreMatch(p1, p3, 9, 11)
                .checkResult(p3, p1, p2)
                .checkTournamentComplete(restState(Lost).bid(p3, Win1)));
    }

    private ImperativeSimulator makeGroupAmbigous(ImperativeSimulator c) {
        return c.beginTournament()
                .scoreSet(p1, 11, p2, 0)
                .scoreSet(p2, 11, p3, 0)
                .scoreSet(p3, 11, p1, 0);
    }

    private ImperativeSimulator makeGroupAmbigous2(ImperativeSimulator c) {
        return c.beginTournament()
                .scoreSet2(p1, 11, p2, 0)
                .scoreSet2(p2, 11, p3, 0)
                .scoreSet2(p3, 11, p1, 0);
    }

    private ImperativeSimulator makeGroupUnambigous2(ImperativeSimulator c) {
        return c.scoreSet2(p1, 11, p2, 0)
                .scoreSet2(p3, 11, p2, 0)
                .scoreSet2(p3, 11, p1, 0);
    }

    private ImperativeSimulator makeGroupUnambigous(ImperativeSimulator c) {
        return c.scoreSet(p1, 11, p2, 0)
                .scoreSet(p3, 11, p2, 0)
                .scoreSet(p3, 11, p1, 0);
    }

    public static TournamentScenario ambigousScenario(String name) {
        return ambigousScenario(name, DM_ORDER_RULES_PUNKTS);
    }

    private static TournamentScenario ambigousScenario(
            String name, List<GroupOrderRule> rules) {
        return TournamentScenario.begin()
                .name(name)
                .rules(RULES_G_S1A2G11_NP.withGroup(
                        RULES_G_S1A2G11_NP.getGroup()
                                .map(g -> g.withOrderRules(rules))))
                .category(c1, p1, p2, p3);
    }

    public static TournamentScenario ambigousScenario(
            String name, TournamentRules rules, List<GroupOrderRule> groupRules) {
        return TournamentScenario.begin()
                .name(name)
                .rules(rules.withGroup(
                        rules.getGroup()
                                .map(g -> g.withOrderRules(groupRules))));
    }

    public static TournamentScenario ambigousScenario3Bids(
            String name, TournamentRules rules) {
        return ambigousScenario(name, rules, DM_ORDER_RULES_PUNKTS)
                .category(c1, p1, p2, p3);
    }

    @Test
    public void rescoreOriginMatchRemovingAmbiguatyMakeOpen() {
        final TournamentScenario scenario = ambigousScenario3Bids(
                "rescoreOriginMakeOpen", RULES_G_S2A2G11_NP);
        final ImperativeSimulator simulator = isf.create(scenario);
        final Map<Set<Player>, Mid> originMatchMap = new HashMap<>();
        final Map<Set<Player>, Mid> dmMatchMap = new HashMap<>();
        simulator.run(c -> makeGroupAmbigous2(c)
                .storeMatchMap(originMatchMap)
                .reloadMatchMap()
                .storeMatchMap(dmMatchMap)
                .and(this::makeGroupUnambigous2)
                .restoreMatchMap(originMatchMap)
                .rescoreMatch(p1, p3, 9, 11)
                .scoreSet(1, p1, 11, p3, 0)
                .scoreSet(2, p1, 11, p3, 0)
                .checkResult(p1, p2, p3)
                .checkTournamentComplete(restState(Lost).bid(p1, Win1)));
    }

    @Test
    public void rescoreOriginMatchRemovingAmbiguatyKeepOver() {
        final TournamentScenario scenario = ambigousScenario(
                "rescoreOriginKeepOver", GroupRulesConst.DM_ORDER_RULES_BALLS);
        final ImperativeSimulator simulator = isf.create(scenario);
        final Map<Set<Player>, Mid> originMatchMap = new HashMap<>();
        final Map<Set<Player>, Mid> dmMatchMap = new HashMap<>();
        simulator.run(c -> makeGroupAmbigous(c)
                .storeMatchMap(originMatchMap)
                .reloadMatchMap()
                .storeMatchMap(dmMatchMap)
                .and(this::makeGroupUnambigous)
                .restoreMatchMap(originMatchMap)
                .rescoreMatch(p1, p3, 11, 9)
                .checkResult(p1, p2, p3)
                .checkTournamentComplete(restState(Lost).bid(p1, Win1)));
    }

    @Test
    public void rescoreOriginMatchChangingAmbiguatyExpand() {
        final TournamentScenario scenario = ambigousScenario(
                "rescoreOriginChangeAmbig", GroupRulesConst.DM_ORDER_RULES_NO_F2F);
        final ImperativeSimulator simulator = isf.create(scenario);
        final Map<Set<Player>, Mid> originMatchMap = new HashMap<>();
        simulator.run(c -> c.beginTournament()
                .scoreSet(p1, 11, p2, 4)
                .scoreSet(p2, 11, p3, 5)
                .scoreSet(p3, 11, p1, 4)
                .storeMatchMap(originMatchMap)
                .reloadMatchMap()
                .scoreSet(p1, 11, p3, 2) // disambiguate match
                .restoreMatchMap(originMatchMap)
                .rescoreMatch(p2, p3, 11, 4)
                .reloadMatchMap()
                .scoreSet(p2, 11, p1, 3) // new dis ma
                .scoreSet(p3, 11, p2, 1) // new dis ma
                .checkResult(p1, p3, p2)
                .checkTournamentComplete(restState(Lost).bid(p1, Win1)));
    }

    @Test
    public void rescoreOriginMatchChangingAmbiguatyShrink() {
        final TournamentScenario scenario = ambigousScenario(
                "rescoreOriginChangeAmbigShrink", GroupRulesConst.DM_ORDER_RULES_NO_F2F);
        final ImperativeSimulator simulator = isf.create(scenario);
        final Map<Set<Player>, Mid> originMatchMap = new HashMap<>();
        simulator.run(c -> c.beginTournament()
                .scoreSet(p1, 11, p2, 4)
                .scoreSet(p2, 11, p3, 4)
                .scoreSet(p3, 11, p1, 4)
                .storeMatchMap(originMatchMap)
                .reloadMatchMap()
                .scoreSet(p1, 11, p3, 2) // disambiguate match
                .restoreMatchMap(originMatchMap)
                .rescoreMatch(p2, p3, 11, 5)
                .checkResult(p1, p3, p2)
                .checkTournamentComplete(restState(Lost).bid(p1, Win1)));
    }

    @Test
    public void rescoreOpenDmMatchKeep() {
        final TournamentScenario scenario = ambigousScenario3Bids(
                "rescoreOpenDmMatchKeep", RULES_G_S2A2G11_NP);
        final ImperativeSimulator simulator = isf.create(scenario);
        simulator.run(c -> makeGroupAmbigous2(c)
                .reloadMatchMap()
                .scoreSet(p1, 11, p2, 0)
                .scoreSet2(p3, 11, p2, 0)
                .scoreSet2(p3, 11, p1, 0)
                .rescoreMatch(p1, p2, 9, 11)
                .scoreSet(1, p1, 11, p2, 0)
                .scoreSet(2, p1, 9, p2, 11)
                .checkResult(p3, p2, p1)
                .checkTournamentComplete(restState(Lost).bid(p3, Win1)));
    }

    @Test
    public void rescoreOpenDmMatchComplete() {
        final TournamentScenario scenario = ambigousScenario3Bids(
                "rescoreOpenDmMatchComplete", RULES_G_S2A2G11_NP);
        final ImperativeSimulator simulator = isf.create(scenario);
        simulator.run(c -> makeGroupAmbigous2(c)
                .reloadMatchMap()
                .scoreSet(p1, 11, p2, 0)
                .scoreSet2(p3, 11, p2, 0)
                .scoreSet2(p3, 11, p1, 0)
                .rescoreMatch(p1, p2, 9, 11, 8, 11)
                .checkResult(p3, p2, p1)
                .checkTournamentComplete(restState(Lost).bid(p3, Win1)));
    }

    @Test
    public void rescoreCompleteDmMatchKeepWinner() {
        final TournamentScenario scenario = ambigousScenario("rescoreCompleteDmKeepWin");
        final ImperativeSimulator simulator = isf.create(scenario);
        simulator.run(c -> makeGroupAmbigous(c)
                .reloadMatchMap()
                .and(this::makeGroupUnambigous)
                .rescoreMatch(p1, p2, 11, 8)
                .checkResult(p3, p1, p2)
                .checkTournamentComplete(restState(Lost).bid(p3, Win1)));
    }

    @Test
    public void rescoreCompleteDmMatchChangeWinner() {
        final TournamentScenario scenario = ambigousScenario("rescoreCompleteDmNewWin");
        final ImperativeSimulator simulator = isf.create(scenario);
        simulator.run(c -> makeGroupAmbigous(c)
                .reloadMatchMap()
                .and(this::makeGroupUnambigous)
                .rescoreMatch(p1, p2, 0, 11)
                .checkResult(p3, p2, p1)
                .checkTournamentComplete(restState(Lost).bid(p3, Win1)));
    }

    @Test
    public void addParticipantToAlmostCompleteGroupWithDm() {
        final TournamentScenario scenario = ambigousScenario("addPlayerCompleteDm");
        final ImperativeSimulator simulator = isf.create(scenario);
        simulator.run(c -> {
            c.and(this::makeGroupAmbigous)
                    .reloadMatchMap()
                    .scoreSet(p1, 11, p2, 0)
                    .scoreSet(p3, 11, p2, 0);

            final Cid cid = scenario.getCategoryDbId().get(c1);

            final GroupPopulations populations = myRest()
                    .get(GROUP_POPULATIONS + scenario.getTid().getTid() + CID + cid,
                            GroupPopulations.class);

            final Bid joinedBid = enlistParticipant(scenario, cid, populations, "p4");
            scenario.addPlayer(joinedBid, p4);
            c.reloadMatchMap()
                    // dm disappeared .scoreSet(p3, 11, p1, 0)
                    .scoreSet(p1, 9, p4, 11)
                    .scoreSet(p2, 3, p4, 11)
                    .scoreSet(p3, 11, p4, 6)
                    .checkResult(p3, p4, p1, p2)
                    .checkTournamentComplete(BidStatesDesc
                            .restState(Lost).bid(p3, Win1));
        });
    }

    private Bid enlistParticipant(TournamentScenario scenario, Cid cid, GroupPopulations populations, String p5) {
        return enlistParticipant(scenario, cid, Optional.of(populations.getLinks().get(0).getGid()), p5);
    }

    private Bid enlistParticipant(TournamentScenario scenario, Cid cid,
            Optional<Gid> gid, String p5) {
        final Uid uid = myRest().post(OFFLINE_USER_REGISTER, scenario,
                OfflineUserRegRequest
                        .builder()
                        .name(p5)
                        .build()).readEntity(Uid.class);

        return myRest().post(TOURNAMENT_ENLIST_OFFLINE, scenario,
                EnlistOffline.builder()
                        .groupId(gid)
                        .uid(uid)
                        .tid(scenario.getTid())
                        .cid(cid)
                        .bidState(BidState.Wait)
                        .build())
                .readEntity(Bid.class);
    }


    @Test
    public void expelParticipantWithDmMatches() {
        final TournamentScenario scenario = ambigousScenario("expelParticipantWithDmMatches");
        final ImperativeSimulator simulator = isf.create(scenario);
        simulator.run(c -> makeGroupAmbigous(c)
                .reloadMatchMap()
                .scoreSet(p1, 11, p2, 0)
                .scoreSet(p3, 11, p2, 0)
                .expelPlayer(p1)
                .checkResult(p3, p1, p2)
                .checkTournamentComplete(restState(Lost)
                        .bid(p1, Expl)
                        .bid(p3, Win1)));
    }

    @Test
    public void expelParticipantFromDmGroup() {
        final TournamentScenario scenario = ambigousScenario(
                "expelParticipantFromDmGroup", GroupRulesConst.DM_ORDER_RULES_NO_F2F);
        final ImperativeSimulator simulator = isf.create(scenario);
        simulator.run(c -> c.beginTournament()
                .scoreSet(p1, 11, p2, 4)
                .scoreSet(p2, 11, p3, 5)
                .scoreSet(p3, 11, p1, 4)
                .reloadMatchMap()
                .expelPlayer(p2)
                .scoreSet(p3, 11, p1, 5) // dm match
                .checkResult(p3, p1, p2)
                .checkTournamentComplete(restState(Lost).bid(p2, Expl).bid(p3, Win1)));
    }

    private ImperativeSimulator checkMatchesP1P2P3Over(ImperativeSimulator c) {
        return c.checkMatchStatus(p1, p2, Over)
                .checkMatchStatus(p1, p3, Over)
                .checkMatchStatus(p3, p2, Over);
    }

    @Test
    public void customRulesForDmMatches() {
        final TournamentScenario scenario = ambigousScenario("customRulesForDmMatches",
                GroupRulesConst.DM_ORDER_RULES_S2A2G11);

        final ImperativeSimulator simulator = isf.create(scenario);
        simulator.run(c -> makeGroupAmbigous(c)
                .and(this::checkMatchesP1P2P3Over)
                .checkTournament(Open, restState(Play))
                .reloadMatchMap()
                .and(this::makeGroupUnambigous2)
                .checkResult(p3, p1, p2)
                .checkTournamentComplete(restState(Lost).bid(p3, Win1)));
    }

    @Test
    public void moveParticipantFromDmGroupToAnother() {
    }

    @Test
    public void moveParticipantWithDmMatchToAnotherGroup() {
    }

    @Test
    public void participantWithDmGetToConsoleTournament() {

    }
}
