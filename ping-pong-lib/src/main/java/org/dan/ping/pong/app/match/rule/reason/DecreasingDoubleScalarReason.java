package org.dan.ping.pong.app.match.rule.reason;

import static java.lang.Double.compare;
import static java.lang.String.format;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.match.rule.OrderRuleName;
import org.dan.ping.pong.util.json.DoubleContextSerializer;
import org.dan.ping.pong.util.json.Precision;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(onConstructor = @__(@JsonIgnore))
public class DecreasingDoubleScalarReason implements Reason {
    @JsonIgnore
    @Getter(onMethod = @__(@JsonIgnore))
    @Setter(onMethod = @__(@JsonIgnore))
    private Bid bid;

    @Precision(4)
    @JsonSerialize(using = DoubleContextSerializer.class)
    private double value;
    private OrderRuleName rule;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DecreasingDoubleScalarReason that = (DecreasingDoubleScalarReason) o;

        if (Double.compare(that.value, value) != 0) {
            return false;
        }
        if (!bid.equals(that.bid)) {
            return false;
        }
        return rule == that.rule;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = bid.hashCode();
        temp = Double.doubleToLongBits(value);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + rule.hashCode();
        return result;
    }

    public static DecreasingDoubleScalarReason ofDoubleD(
            double n, OrderRuleName rule) {
        return ofDoubleD(null, n, rule);
    }

    public static double nanAs0(double d) {
        if (Double.isNaN(d)) {
            return 0.0;
        }
        return d;
    }

    public static DecreasingDoubleScalarReason ofDoubleD(
            Bid bid, double n, OrderRuleName rule) {
        return new DecreasingDoubleScalarReason(bid, n, rule);
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
