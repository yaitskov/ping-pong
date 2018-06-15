package org.dan.ping.pong.app.match.rule.reason;

import static org.dan.ping.pong.app.match.rule.service.GroupOrderRuleServiceCtx.F2F;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.match.rule.OrderRuleName;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(value = F2fReason.class, name = F2F),
        @JsonSubTypes.Type(value = InfoReason.class, name = "INF"),
        @JsonSubTypes.Type(value = DecreasingLongScalarReason.class, name = "DL"),
        @JsonSubTypes.Type(value = IncreasingIntScalarReason.class, name = "II"),
        @JsonSubTypes.Type(value = DecreasingDoubleScalarReason.class, name = "DD"),
        @JsonSubTypes.Type(value = WeightSetsReason.class, name = "WSR"),
        @JsonSubTypes.Type(value = DecreasingIntScalarReason.class, name = "DI")})
public interface Reason extends Comparable<Reason> {
    TypeReference<List<Reason>> REASON_CHAIN_TYPE = new TypeReference<List<Reason>>() {};

    Uid getUid();

    OrderRuleName getRule();
    void setRule(OrderRuleName rule);
}
