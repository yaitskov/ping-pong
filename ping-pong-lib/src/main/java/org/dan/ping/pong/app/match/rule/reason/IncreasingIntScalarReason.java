package org.dan.ping.pong.app.match.rule.reason;

import lombok.NoArgsConstructor;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.match.rule.OrderRuleName;

import java.util.Map;

@NoArgsConstructor
public class IncreasingIntScalarReason extends DecreasingIntScalarReason {
    public IncreasingIntScalarReason(Bid bid, int n, OrderRuleName rule) {
        super(bid, n, rule);
    }

    public static IncreasingIntScalarReason ofIntI(int n, OrderRuleName rule) {
        return ofIntI(null, n, rule);
    }

    public static IncreasingIntScalarReason ofIntI(Bid bid, int n, OrderRuleName rule) {
        return new IncreasingIntScalarReason(bid, n, rule);
    }

    public static IncreasingIntScalarReason ofEntry(Map.Entry<Bid, Integer> e,
            OrderRuleName rule) {
        return ofIntI(e.getKey(), e.getValue(), rule);
    }

    public int compareTo(Reason r) {
        return -super.compareTo(r);
    }

    protected String pattern() {
        return "%s: +%d";
    }
}
