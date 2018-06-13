package org.dan.ping.pong.app.match.rule.reason;

import static org.dan.ping.pong.util.collection.IterableComparator.LengthPolicy.LongerSmaller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.group.HisIntPair;
import org.dan.ping.pong.app.match.rule.OrderRuleName;
import org.dan.ping.pong.util.collection.CmpValueCounter;
import org.dan.ping.pong.util.collection.IterableComparator;

import java.util.TreeSet;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor(onConstructor = @__(@JsonIgnore))
public class WeightSetsReason implements Reason {
    private static final IterableComparator<CmpValueCounter<HisIntPair>> WEIGHT_SET_CMP
            = new IterableComparator<>(CmpValueCounter::compareTo);

    @JsonIgnore
    @Getter(onMethod = @__(@JsonIgnore))
    @Setter(onMethod = @__(@JsonIgnore))
    private Uid uid;
    private OrderRuleName rule;
    private TreeSet<CmpValueCounter<HisIntPair>> weightSets;

    @Override
    public int compareTo(Reason o) {
        return WEIGHT_SET_CMP.compare(
                weightSets,
                ((WeightSetsReason) o).weightSets,
                LongerSmaller);
    }
}
