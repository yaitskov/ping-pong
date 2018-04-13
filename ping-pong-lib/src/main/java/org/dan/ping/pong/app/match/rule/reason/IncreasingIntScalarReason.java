package org.dan.ping.pong.app.match.rule.reason;

import lombok.NoArgsConstructor;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.match.rule.OrderRuleName;

import java.util.Map;

@NoArgsConstructor
public class IncreasingIntScalarReason extends DecreasingIntScalarReason {
    public IncreasingIntScalarReason(Uid uid, int n, OrderRuleName rule) {
        super(uid, n, rule);
    }

    public static IncreasingIntScalarReason ofIntI(Uid uid, int n, OrderRuleName rule) {
        return new IncreasingIntScalarReason(uid, n, rule);
    }

    public static IncreasingIntScalarReason ofEntry(Map.Entry<Uid, Integer> e,
            OrderRuleName rule) {
        return ofIntI(e.getKey(), e.getValue(), rule);
    }

    public int compareTo(Reason r) {
        return -super.compareTo(r);
    }
}
