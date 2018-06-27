package org.dan.ping.pong.app.match.rule.reason;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.match.rule.OrderRuleName;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(onConstructor = @__(@JsonIgnore))
public class InfoReason implements Reason {
    @Getter(onMethod = @__(@JsonIgnore))
    private Bid bid;
    private OrderRuleName rule;

    @JsonIgnore
    public InfoReason(OrderRuleName rule) {
        this.rule = rule;
    }

    public static InfoReason notApplicableRule(OrderRuleName rule) {
        return notApplicableRule(null, rule);
    }

    public static InfoReason notApplicableRule(Bid uid, OrderRuleName rule) {
        return new InfoReason(uid, rule);
    }

    @Override
    public int compareTo(Reason o) {
        if (o instanceof InfoReason) {
            return 0;
        }
        throw new IllegalStateException();
    }

    public String toString() {
        return "skip " + rule.name();
    }

    public boolean equals(Object o) {
        if (o instanceof InfoReason) {
            return rule == ((InfoReason) o).rule;
        }
        return false;
    }

    public int hashCode() {
        return rule.hashCode();
    }
}
