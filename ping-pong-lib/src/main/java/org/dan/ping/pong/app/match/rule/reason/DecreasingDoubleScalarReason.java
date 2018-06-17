package org.dan.ping.pong.app.match.rule.reason;

import static java.lang.Double.compare;
import static java.lang.String.format;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.match.rule.OrderRuleName;
import org.dan.ping.pong.util.json.DoubleContextSerializer;
import org.dan.ping.pong.util.json.Precision;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(onConstructor = @__(@JsonIgnore))
public class DecreasingDoubleScalarReason implements Reason {
    @JsonIgnore
    @Getter(onMethod = @__(@JsonIgnore))
    @Setter(onMethod = @__(@JsonIgnore))
    private Uid uid;

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
        if (!uid.equals(that.uid)) {
            return false;
        }
        return rule == that.rule;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = uid.hashCode();
        temp = Double.doubleToLongBits(value);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + rule.hashCode();
        return result;
    }

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
