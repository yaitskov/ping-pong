package org.dan.ping.pong.app.group;

import static java.util.Arrays.asList;
import static org.dan.ping.pong.app.bid.BidState.Lost;
import static org.dan.ping.pong.app.bid.BidState.Play;
import static org.dan.ping.pong.app.bid.BidState.Win1;
import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_G_S1A2G11_NP;
import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_G_S2A2G11_NP;
import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_G_S3A2G11_NP;
import static org.dan.ping.pong.app.match.MatchState.Over;
import static org.dan.ping.pong.app.match.rule.rules.common.DirectOutcomeRule.DIRECT_OUTCOME_RULE;
import static org.dan.ping.pong.app.tournament.TournamentState.Open;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.dan.ping.pong.mock.simulator.imerative.BidStatesDesc.restState;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.match.Mid;
import org.dan.ping.pong.app.match.rule.rules.GroupOrderRule;
import org.dan.ping.pong.app.match.rule.rules.common.BallsBalanceRule;
import org.dan.ping.pong.app.match.rule.rules.common.PickRandomlyRule;
import org.dan.ping.pong.app.match.rule.rules.meta.UseDisambiguationMatchesDirective;
import org.dan.ping.pong.app.match.rule.rules.ping.CountJustPunktsRule;
import org.dan.ping.pong.app.tournament.JerseyWithSimulator;
import org.dan.ping.pong.app.tournament.TournamentRules;
import org.dan.ping.pong.mock.simulator.Player;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.dan.ping.pong.mock.simulator.imerative.ImperativeSimulator;
import org.dan.ping.pong.mock.simulator.imerative.ImperativeSimulatorFactory;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class DisambiguationMatchJerseyTest extends AbstractSpringJerseyTest {
    private static final List<GroupOrderRule> DM_ORDER_RULES_PUNKTS =
            asList(new CountJustPunktsRule(),
                    DIRECT_OUTCOME_RULE,
                    new UseDisambiguationMatchesDirective(),
                    new CountJustPunktsRule(),
                    DIRECT_OUTCOME_RULE,
                    new PickRandomlyRule());

    private static final List<GroupOrderRule> DM_ORDER_RULES_BALLS =
            asList(new CountJustPunktsRule(),
                    DIRECT_OUTCOME_RULE,
                    new BallsBalanceRule(),
                    DIRECT_OUTCOME_RULE,
                    new UseDisambiguationMatchesDirective(),
                    new CountJustPunktsRule(),
                    DIRECT_OUTCOME_RULE,
                    new BallsBalanceRule(),
                    DIRECT_OUTCOME_RULE,
                    new PickRandomlyRule());

    @Inject
    private ImperativeSimulatorFactory isf;

    @Test
    public void genDisambiguationMatchesSameMatchRule() {
        final TournamentScenario scenario = ambigousScenario("genDisambiguationMatches");

        final ImperativeSimulator simulator = isf.create(scenario);
        simulator.run(c -> makeGroupAmbigous(c)
                .checkMatchStatus(p1, p2, Over)
                .checkMatchStatus(p1, p3, Over)
                .checkMatchStatus(p3, p2, Over)
                .checkTournament(Open, restState(Play))
                .reloadMatchMap()
                .scoreSet(p1, 11, p2, 0)
                .scoreSet(p3, 11, p2, 0)
                .scoreSet(p3, 11, p1, 0)
                .checkResult(p3, p1, p2)
                .checkTournamentComplete(restState(Lost).bid(p3, Win1)));
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
                .scoreSet(p1, 11, p2, 0)
                .scoreSet(p3, 11, p2, 0)
                .scoreSet(p3, 11, p1, 0)
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

    private TournamentScenario ambigousScenario(String name) {
        return ambigousScenario(name, DM_ORDER_RULES_PUNKTS);
    }

    private TournamentScenario ambigousScenario(String name, List<GroupOrderRule> rules) {
        return TournamentScenario.begin()
                .name(name)
                .rules(RULES_G_S1A2G11_NP.withGroup(
                        RULES_G_S1A2G11_NP.getGroup()
                                .map(g -> g.withOrderRules(rules))))
                .category(c1, p1, p2, p3);
    }

    private TournamentScenario ambigousScenario(String name, TournamentRules rules) {
        return TournamentScenario.begin()
                .name(name)
                .rules(rules.withGroup(
                        rules.getGroup()
                                .map(g -> g.withOrderRules(DM_ORDER_RULES_PUNKTS))))
                .category(c1, p1, p2, p3);
    }

    @Test
    public void rescoreOriginMatchRemovingAmbiguatyMakeOpen() {
        final TournamentScenario scenario = ambigousScenario(
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
                "rescoreOriginKeepOver", DM_ORDER_RULES_BALLS);
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
    public void rescoreOriginMatchChangingAmbiguaty() {
    }

    @Test
    public void rescoreOpenDmMatchKeep() {
    }

    @Test
    public void rescoreOpenDmMatchComplete() {
    }

    @Test
    public void rescoreCompleteDmMatchKeepWinner() {
    }

    @Test
    public void rescoreCompleteDmMatchChangeWinner() {
    }

    @Test
    public void addParticipantToCompleteGroupWithDm() {
    }

    @Test
    public void expelParticipantWithDmMatches() {
    }

    @Test
    public void expelParticipantFromDmGroup() {
    }

    @Test
    public void moveParticipantFromDmGroupToAnother() {
    }

    @Test
    public void moveParticipantWithDmMatchToAnotherGroup() {
    }
}
