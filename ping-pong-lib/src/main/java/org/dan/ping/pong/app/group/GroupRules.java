package org.dan.ping.pong.app.group;

import static java.util.Arrays.asList;
import static org.dan.ping.pong.app.group.ConsoleTournament.NO;
import static org.dan.ping.pong.app.match.rule.rules.common.DirectOutcomeRule.DIRECT_OUTCOME_RULE;
import static org.dan.ping.pong.app.match.rule.rules.common.PickRandomlyRule.PICK_RANDOMLY_RULE;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Wither;
import org.dan.ping.pong.app.match.rule.rules.GroupOrderRule;
import org.dan.ping.pong.app.match.rule.rules.common.BallsBalanceRule;
import org.dan.ping.pong.app.match.rule.rules.common.CountWonMatchesRule;
import org.dan.ping.pong.app.match.rule.rules.common.LostBallsRule;
import org.dan.ping.pong.app.match.rule.rules.common.LostSetsRule;
import org.dan.ping.pong.app.match.rule.rules.common.SetsBalanceRule;
import org.dan.ping.pong.app.match.rule.rules.common.WonBallsRule;
import org.dan.ping.pong.app.match.rule.rules.common.WonSetsRule;

import java.util.List;
import java.util.Optional;

@Getter
@Setter
@Wither
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GroupRules {
    public static final List<GroupOrderRule> BALANCE_BASED_ORDER_RULES =
            asList(new CountWonMatchesRule(),
                    DIRECT_OUTCOME_RULE,
                    new SetsBalanceRule(),
                    DIRECT_OUTCOME_RULE,
                    new BallsBalanceRule(),
                    DIRECT_OUTCOME_RULE,
                    PICK_RANDOMLY_RULE);

    public static final List<GroupOrderRule> WON_LOST_BASED_ORDER_RULES =
            asList(new CountWonMatchesRule(),
                    DIRECT_OUTCOME_RULE,
                    new WonSetsRule(),
                    new LostSetsRule(),
                    DIRECT_OUTCOME_RULE,
                    new WonBallsRule(),
                    new LostBallsRule(),
                    DIRECT_OUTCOME_RULE,
                    PICK_RANDOMLY_RULE);

    private int quits;
    private int groupSize;
    private ConsoleTournament console = NO;
    private Optional<DisambiguateMatchRules> disambiguationMatch = Optional.empty();
    private List<GroupOrderRule> orderRules = BALANCE_BASED_ORDER_RULES;

    /**
     * Default means {@link GroupSchedule#DEFAULT_SCHEDULE}
     */
    private Optional<GroupSchedule> schedule = Optional.empty();

    public static class GroupRulesBuilder {
        Optional<GroupSchedule> schedule = Optional.empty();
        ConsoleTournament console = NO;
        Optional<DisambiguateMatchRules> disambiguationMatch = Optional.empty();
        List<GroupOrderRule> orderRules = BALANCE_BASED_ORDER_RULES;
    }
}
