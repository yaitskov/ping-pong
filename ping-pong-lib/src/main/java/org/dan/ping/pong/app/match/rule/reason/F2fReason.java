package org.dan.ping.pong.app.match.rule.reason;

import static java.lang.String.format;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.F2F;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.match.rule.OrderRuleName;

@Getter
@Setter
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor(onConstructor = @__(@JsonIgnore))
public class F2fReason implements Reason {
    private int won;
    private Bid bid;
    private Bid opponentBid;

    @Override
    public int compareTo(Reason r) {
        return ((F2fReason) r).won - won;
    }

    private static final String[] WON_PATTERNS = new String[] {
            "F2F: %d > %d",
            "F2F: %d < %d"
    };

    public String toString() {
        return format(WON_PATTERNS[won], bid.intValue(), opponentBid.intValue());
    }

    @Override
    public OrderRuleName getRule() {
        return F2F;
    }

    @Override
    public void setRule(OrderRuleName rule) {
        // skip
    }
}
