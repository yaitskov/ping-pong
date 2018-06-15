package org.dan.ping.pong.app.match.rule.reason;

import static java.lang.Double.compare;
import static java.lang.String.format;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.match.rule.OrderRuleName;

import java.util.Map;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor(onConstructor = @__(@JsonIgnore))
public class DecreasingDoubleScalarReason implements Reason {
    @JsonIgnore
    @Getter(onMethod = @__(@JsonIgnore))
    @Setter(onMethod = @__(@JsonIgnore))
    private Uid uid;
    private double value;
    private OrderRuleName rule;

    public static DecreasingDoubleScalarReason ofDoubleD(
            double n, OrderRuleName rule) {
        return ofDoubleD(null, n, rule);
    }

    public static DecreasingDoubleScalarReason ofDoubleD(
            Uid uid, double n, OrderRuleName rule) {
        return new DecreasingDoubleScalarReason(uid, n, rule);
    }

    public static DecreasingDoubleScalarReason ofEntry(
            Map.Entry<Uid, Integer> e,
            OrderRuleName rule) {
        return ofDoubleD(e.getKey(), e.getValue(), rule);
    }

    @Override
    public int compareTo(Reason r) {
        return -compare(value, ((DecreasingDoubleScalarReason) r).value);
    }

    public String toString() {
        return format(pattern(), rule.name(), value);
    }

    protected String pattern() {
        return "%s: -%.2f";
    }
}
