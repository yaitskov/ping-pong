package org.dan.ping.pong.app.match.rule.reason;

import static java.lang.String.format;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.F2F;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.match.rule.OrderRuleName;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class F2fReason implements Reason {
    private int won;
    private Uid uid;
    private Uid opponentUid;
    private OrderRuleName rule = F2F;

    @Override
    public int compareTo(Reason r) {
        return ((F2fReason) r).won - won;
    }

    private static final String[] WON_PATTERNS = new String[] {
            "F2F: %d > %d",
            "F2F: %d < %d"
    };

    public String toString() {
        return format(WON_PATTERNS[won], uid.getId(), opponentUid.getId());
    }
}
