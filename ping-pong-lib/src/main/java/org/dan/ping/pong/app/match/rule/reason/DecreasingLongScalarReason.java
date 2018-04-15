package org.dan.ping.pong.app.match.rule.reason;


import static java.lang.Long.compare;
import static java.lang.String.format;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.match.rule.OrderRuleName;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DecreasingLongScalarReason implements Reason {
    @JsonIgnore
    @Getter(onMethod = @__(@JsonIgnore))
    @Setter(onMethod = @__(@JsonIgnore))
    private Uid uid;
    private long value;
    private OrderRuleName rule;

    public static DecreasingLongScalarReason ofLongD(Uid uid, long n, OrderRuleName rule) {
        return new DecreasingLongScalarReason(uid, n, rule);
    }

    @Override
    public int compareTo(Reason r) {
        return -compare(value, ((DecreasingLongScalarReason) r).value);
    }

    public String toString() {
        return format("%s: %d", rule.name(), value);
    }
}
