package org.dan.ping.pong.app.match.rule.rules;

import static org.dan.ping.pong.app.match.rule.service.GroupOrderRuleServiceCtx.COUNT_DM_MATCHES;
import static org.dan.ping.pong.app.match.rule.service.GroupOrderRuleServiceCtx.COUNT_JUST_PUNKTS;
import static org.dan.ping.pong.app.match.rule.service.GroupOrderRuleServiceCtx.COUNT_WON_MATCHES;
import static org.dan.ping.pong.app.match.rule.service.GroupOrderRuleServiceCtx.F2F;
import static org.dan.ping.pong.app.match.rule.service.GroupOrderRuleServiceCtx.RANDOM;
import static org.dan.ping.pong.app.match.rule.service.GroupOrderRuleServiceCtx.USE_BALLS;
import static org.dan.ping.pong.app.match.rule.service.GroupOrderRuleServiceCtx.USE_SETS;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.dan.ping.pong.app.match.rule.OrderRuleName;
import org.dan.ping.pong.app.match.rule.rules.attrs.CompletenessScoped;
import org.dan.ping.pong.app.match.rule.rules.attrs.ParticipantScoped;
import org.dan.ping.pong.app.match.rule.rules.common.DirectOutcomeRule;
import org.dan.ping.pong.app.match.rule.rules.common.CountWonMatchesRule;
import org.dan.ping.pong.app.match.rule.rules.common.PickRandomlyRule;
import org.dan.ping.pong.app.match.rule.rules.common.BallsBalanceRule;
import org.dan.ping.pong.app.match.rule.rules.common.SetsBalanceRule;
import org.dan.ping.pong.app.match.rule.rules.meta.UseDisambiguationMatchesDirective;
import org.dan.ping.pong.app.match.rule.rules.ping.CountJustPunktsRule;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PickRandomlyRule.class, name = RANDOM),
        @JsonSubTypes.Type(value = DirectOutcomeRule.class, name = F2F),
        @JsonSubTypes.Type(value = CountJustPunktsRule.class, name = COUNT_JUST_PUNKTS),
        @JsonSubTypes.Type(value = UseDisambiguationMatchesDirective.class, name = COUNT_DM_MATCHES),
        @JsonSubTypes.Type(value = SetsBalanceRule.class, name = USE_SETS),
        @JsonSubTypes.Type(value = BallsBalanceRule.class, name = USE_BALLS),
        @JsonSubTypes.Type(value = CountWonMatchesRule.class, name = COUNT_WON_MATCHES)
})
public interface GroupOrderRule extends CompletenessScoped, ParticipantScoped {
    OrderRuleName name();
}
