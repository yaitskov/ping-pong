package org.dan.ping.pong.app.match.rule.reason;

import static org.dan.ping.pong.app.match.rule.service.GroupOrderRuleServiceCtx.F2F;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.match.rule.OrderRuleName;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(value = F2fReason.class, name = F2F),
        @JsonSubTypes.Type(value = InfoReason.class, name = "INF"),
        @JsonSubTypes.Type(value = IncreasingIntScalarReason.class, name = "INC"),
        @JsonSubTypes.Type(value = DecreasingIntScalarReason.class, name = "DEC")})
public interface Reason extends Comparable<Reason> {
    Uid getUid();

    OrderRuleName getRule();
    void setRule(OrderRuleName rule);
}
