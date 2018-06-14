package org.dan.ping.pong.app.tournament.rules;

import static java.util.stream.Collectors.toMap;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.AtpDI;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.AtpWeightedMatches;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.BallsBalance;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.UidOrder;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.UseDisambiguationMatches;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.F2F;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.LostBalls;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.LostSets;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.Punkts;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.Random;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.SetsBalance;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.WeightedMatches;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.WonBalls;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.WonMatches;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.WonSets;
import static org.dan.ping.pong.app.tournament.rules.ValidationError.ofTemplate;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import org.dan.ping.pong.app.group.GroupRules;
import org.dan.ping.pong.app.group.GroupSchedule;
import org.dan.ping.pong.app.match.rule.OrderRuleName;
import org.dan.ping.pong.app.match.rule.rules.GroupOrderRule;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GroupRuleValidator {
    public static final String GROUP_RULE = "group-rule";
    public static final String MAX_SIZE = ".max-size";
    public static final String OUT_OF_RANGE = "out-of-range";
    public static final String QUITS = ".quits";
    public static final String VALUE_NULL = "value-null";
    public static final String SCHEDULE = ".schedule";
    static final String UNEXPECTED_MATCHES = "unexpected-matches";
    public static final String MISSING_MATCHES = "missing-matches";
    static final String MATCHES = "matches";
    public static final String ORDER_RULES = ".order-rules";

    private static final Set<OrderRuleName> GROUP_RULE_NAMES = ImmutableSet.of(
            Punkts,
            UseDisambiguationMatches,
            WeightedMatches,
            AtpDI,
            AtpWeightedMatches,
            F2F,
            Random,
            UidOrder,
            SetsBalance,
            BallsBalance,
            WonSets,
            LostSets,
            WonBalls,
            LostBalls,
            WonMatches);

    public void validate(Multimap<String, ValidationError> errors, GroupRules group) {
        if (group == null) {
            errors.put(GROUP_RULE, ofTemplate(VALUE_NULL));
            return;
        }
        if (group.getGroupSize() < 2 || group.getGroupSize() > 20) {
            errors.put(GROUP_RULE + MAX_SIZE, ofTemplate(OUT_OF_RANGE));
        }
        if (group.getQuits() < 1 || group.getQuits() >= group.getGroupSize()) {
            errors.put(GROUP_RULE + QUITS, ofTemplate(OUT_OF_RANGE));
        }
        group.getSchedule().ifPresent(schedule -> validateSchedule(schedule, errors));
        validateOrderRules(errors, group);
    }

    private void validateOrderRules(Multimap<String, ValidationError> errors, GroupRules group) {
        if (group.getOrderRules().isEmpty()) {
            errors.put(GROUP_RULE + ORDER_RULES,
                    ofTemplate("list-empty"));
        } else if (group.getOrderRules().get(group.getOrderRules().size() - 1).name() != Random) {
            errors.put(GROUP_RULE + ORDER_RULES,
                    ofTemplate("last-group-order-rule-is-not-random"));
        } else if (group.getOrderRules().size() > 21) {
            errors.put(GROUP_RULE + ORDER_RULES, ofTemplate("to-many-values"));
        } else {
            final Map<OrderRuleName, Integer> ruleCounter = group.getOrderRules()
                    .stream()
                    .collect(toMap(GroupOrderRule::name, o -> 1, (a, b) -> a + b));
            if (ruleCounter.get(Random) != 1) {
                errors.put(GROUP_RULE + ORDER_RULES, ofTemplate("one-random-rule-is-allowed"));
            }
            ruleCounter.forEach((ruleName, count) -> {
                if (count > 6) {
                    errors.put(GROUP_RULE + ORDER_RULES,
                            ofTemplate("to-many-rules-of",
                                    ImmutableMap.of("rule", ruleName)));
                } else if (!GROUP_RULE_NAMES.contains(ruleName)) {
                    errors.put(GROUP_RULE + ORDER_RULES,
                            ofTemplate("rule-not-applicable-for-group",
                                    ImmutableMap.of("rule", ruleName)));
                }
            });
        }
    }

    private void validateSchedule(GroupSchedule schedule,
            Multimap<String, ValidationError> errors) {
        if (schedule.getSize2Schedule() == null) {
            errors.put(GROUP_RULE + SCHEDULE, ofTemplate(VALUE_NULL));
            return;
        }
        schedule.getSize2Schedule().forEach((n, matches) -> {
            if (matches.size() % 2 == 1) {
                errors.put(GROUP_RULE + SCHEDULE,
                        ofTemplate("schedule-n-odd", "n", n));
            } else {
                validateScheduleLine(errors, n, matches);
            }
        });
    }

    void validateScheduleLine(
            Multimap<String, ValidationError> errors,
            int n, List<Integer> matches) {
        final Set<Set<Integer>> setOfPairs = new HashSet<>();
        for (int i = 0; i < matches.size();) {
            setOfPairs.add(ImmutableSet.of(matches.get(i++), matches.get(i++)));
        }
        final Set<Set<Integer>> setOfExpectedPairs = new HashSet<>();
        for (int i = 0; i < n; ++i) {
            for (int j = i + 1; j < n; ++j) {
                setOfExpectedPairs.add(ImmutableSet.of(i, j));
            }
        }
        final Set<Set<Integer>> unexpectedMatches =  new HashSet<>(setOfPairs);
        unexpectedMatches.removeAll(setOfExpectedPairs);
        if (!unexpectedMatches.isEmpty()) {
            errors.put(GROUP_RULE + SCHEDULE,
                    ofTemplate(UNEXPECTED_MATCHES, MATCHES, unexpectedMatches, n));
            return;
        }
        setOfExpectedPairs.removeAll(setOfPairs);
        if (!setOfExpectedPairs.isEmpty()) {
            errors.put(GROUP_RULE + SCHEDULE,
                    ofTemplate(MISSING_MATCHES, MATCHES, setOfExpectedPairs, n));
        }
    }
}
