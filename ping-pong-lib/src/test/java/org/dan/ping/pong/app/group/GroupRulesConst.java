package org.dan.ping.pong.app.group;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.dan.ping.pong.app.group.GroupRules.BALANCE_BASED_ORDER_RULES;
import static org.dan.ping.pong.app.group.GroupRules.WON_LOST_BASED_ORDER_RULES;
import static org.dan.ping.pong.app.match.MatchRulesConst.S2A2G11;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.UseDisambiguationMatches;
import static org.dan.ping.pong.app.match.rule.rules.common.DirectOutcomeRule.DIRECT_OUTCOME_RULE;

import org.dan.ping.pong.app.group.GroupRules;
import org.dan.ping.pong.app.match.rule.rules.GroupOrderRule;
import org.dan.ping.pong.app.match.rule.rules.common.BallsBalanceRule;
import org.dan.ping.pong.app.match.rule.rules.common.LostBallsRule;
import org.dan.ping.pong.app.match.rule.rules.common.OrderByUidRule;
import org.dan.ping.pong.app.match.rule.rules.common.PickRandomlyRule;
import org.dan.ping.pong.app.match.rule.rules.meta.UseDisambiguationMatchesDirective;
import org.dan.ping.pong.app.match.rule.rules.ping.CountJustPunktsRule;

import java.util.List;
import java.util.Optional;

public class GroupRulesConst {
    public static final GroupRules G8Q1 = GroupRules.builder()
            .groupSize(8)
            .quits(1)
            .orderRules(WON_LOST_BASED_ORDER_RULES)
            .build();

    public static final GroupRules G8Q2 = G8Q1.withQuits(2);
    public static final GroupRules G8Q2_M = G8Q2.withOrderRules(BALANCE_BASED_ORDER_RULES);
    public static final GroupRules G3Q2 = G8Q2.withGroupSize(3);
    public static final GroupRules G3Q1 = G3Q2.withQuits(1);
    public static final GroupRules G2Q1 = G3Q1.withGroupSize(2);

    public static final List<GroupOrderRule> DM_ORDER_RULES_PUNKTS =
            asList(new CountJustPunktsRule(),
                    DIRECT_OUTCOME_RULE,
                    new UseDisambiguationMatchesDirective(),
                    new CountJustPunktsRule(),
                    DIRECT_OUTCOME_RULE,
                    new PickRandomlyRule());

    public static final List<GroupOrderRule> DM_ORDER_RULES_S2A2G11 = DM_ORDER_RULES_PUNKTS.stream()
            .map(gor -> {
                if (gor.name() == UseDisambiguationMatches) {
                    return new UseDisambiguationMatchesDirective(Optional.of(S2A2G11));
                }
                return gor;
            })
            .collect(toList());

    public static final List<GroupOrderRule> DM_ORDER_RULES_BALLS =
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

    public static final List<GroupOrderRule> DM_ORDER_RULES_NO_F2F =
            asList(new LostBallsRule(),
                    new UseDisambiguationMatchesDirective(),
                    new BallsBalanceRule(),
                    new OrderByUidRule());
}
