package org.dan.ping.pong.app.match.rule.reason;

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

import java.util.Collection;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor(onConstructor = @__(@JsonIgnore))
public class WeightSetsReason implements Reason {
    @JsonIgnore
    @Getter(onMethod = @__(@JsonIgnore))
    @Setter(onMethod = @__(@JsonIgnore))
    private Uid uid;
    private OrderRuleName rule;
    private Collection<CmpValueCounter<HisIntPair>> weightSets;

    @Override
    public int compareTo(Reason o) {
        throw new IllegalStateException();
    }
}
