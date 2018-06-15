package org.dan.ping.pong.app.match.rule.rules;

import static org.dan.ping.pong.app.match.rule.service.GroupOrderRuleServiceCtx.DM_MATCHES;
import static org.dan.ping.pong.app.match.rule.service.GroupOrderRuleServiceCtx.PUNKTS;
import static org.dan.ping.pong.app.match.rule.service.GroupOrderRuleServiceCtx.WON_MATCHES;
import static org.dan.ping.pong.app.match.rule.service.GroupOrderRuleServiceCtx.F2F;
import static org.dan.ping.pong.app.match.rule.service.GroupOrderRuleServiceCtx.RANDOM;
import static org.dan.ping.pong.app.match.rule.service.GroupOrderRuleServiceCtx.BALLS_BALANCE;
import static org.dan.ping.pong.app.match.rule.service.GroupOrderRuleServiceCtx.SETS_BALANCE;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.dan.ping.pong.app.match.rule.OrderRuleName;
import org.dan.ping.pong.app.match.rule.filter.DisambiguationScope;
import org.dan.ping.pong.app.match.rule.rules.attrs.CompletenessScoped;
import org.dan.ping.pong.app.match.rule.rules.attrs.ParticipantScoped;
import org.dan.ping.pong.app.match.rule.rules.tennis.atp.AtpDIIPercentWonSetsRule;
import org.dan.ping.pong.app.match.rule.rules.tennis.atp.AtpDIIIPercentWonGamesRule;
import org.dan.ping.pong.app.match.rule.rules.tennis.atp.AtpWeightedMatchesBySetsRule;
import org.dan.ping.pong.app.match.rule.rules.common.DirectOutcomeRule;
import org.dan.ping.pong.app.match.rule.rules.common.CountWonMatchesRule;
import org.dan.ping.pong.app.match.rule.rules.common.LostBallsRule;
import org.dan.ping.pong.app.match.rule.rules.common.LostSetsRule;
import org.dan.ping.pong.app.match.rule.rules.common.OrderByUidRule;
import org.dan.ping.pong.app.match.rule.rules.common.PickRandomlyRule;
import org.dan.ping.pong.app.match.rule.rules.common.BallsBalanceRule;
import org.dan.ping.pong.app.match.rule.rules.common.SetsBalanceRule;
import org.dan.ping.pong.app.match.rule.rules.common.WeightedMatchesBySetsRule;
import org.dan.ping.pong.app.match.rule.rules.common.WonBallsRule;
import org.dan.ping.pong.app.match.rule.rules.common.WonSetsRule;
import org.dan.ping.pong.app.match.rule.rules.meta.UseDisambiguationMatchesDirective;
import org.dan.ping.pong.app.match.rule.rules.ping.CountJustPunktsRule;
import org.dan.ping.pong.app.match.rule.rules.tennis.atp.AtpDIRule;

import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(value = BallsBalanceRule.class, name = BALLS_BALANCE),
        @JsonSubTypes.Type(value = CountWonMatchesRule.class, name = WON_MATCHES),
        @JsonSubTypes.Type(value = DirectOutcomeRule.class, name = F2F),
        @JsonSubTypes.Type(value = LostBallsRule.class, name = "LB"),
        @JsonSubTypes.Type(value = LostSetsRule.class, name = "LS"),
        @JsonSubTypes.Type(value = PickRandomlyRule.class, name = RANDOM),
        @JsonSubTypes.Type(value = OrderByUidRule.class, name = "UO"),
        @JsonSubTypes.Type(value = SetsBalanceRule.class, name = SETS_BALANCE),
        @JsonSubTypes.Type(value = WonBallsRule.class, name = "WB"),
        @JsonSubTypes.Type(value = WonSetsRule.class, name = "WS"),
        @JsonSubTypes.Type(value = WeightedMatchesBySetsRule.class, name = "WtMbS"),
        @JsonSubTypes.Type(value = AtpWeightedMatchesBySetsRule.class, name = "AtpWMS"),
        @JsonSubTypes.Type(value = AtpDIRule.class, name = "AtpDI"),
        @JsonSubTypes.Type(value = AtpDIIPercentWonSetsRule.class, name = "AtpDII"),
        @JsonSubTypes.Type(value = AtpDIIIPercentWonGamesRule.class, name = "AtpDIII"),
        @JsonSubTypes.Type(value = UseDisambiguationMatchesDirective.class, name = DM_MATCHES),

        @JsonSubTypes.Type(value = CountJustPunktsRule.class, name = PUNKTS)
})
public interface GroupOrderRule extends CompletenessScoped, ParticipantScoped {
    OrderRuleName name();

    default Optional<DisambiguationScope> disambiguationScope() {
        return Optional.empty();
    }
}
