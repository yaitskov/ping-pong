package org.dan.ping.pong.app.match.rule.reason;

import static java.lang.Integer.compare;
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
public class DecreasingIntScalarReason implements Reason {
    @JsonIgnore
    @Getter(onMethod = @__(@JsonIgnore))
    @Setter(onMethod = @__(@JsonIgnore))
    private Uid uid;
    private int value;
    private OrderRuleName rule;

    public static DecreasingIntScalarReason ofIntD(int n, OrderRuleName rule) {
        return ofIntD(null, n, rule);
    }

    public static DecreasingIntScalarReason ofIntD(Uid uid, int n, OrderRuleName rule) {
        return new DecreasingIntScalarReason(uid, n, rule);
    }

    public static DecreasingIntScalarReason ofEntry(Map.Entry<Uid, Integer> e,
            OrderRuleName rule) {
        return ofIntD(e.getKey(), e.getValue(), rule);
    }

    @Override
    public int compareTo(Reason r) {
        return -compare(value, ((DecreasingIntScalarReason) r).value);
    }

    public String toString() {
        return format("%s: %d", rule.name(), value);
    }
}
