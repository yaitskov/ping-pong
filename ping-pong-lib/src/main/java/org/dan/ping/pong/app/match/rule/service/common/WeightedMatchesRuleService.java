package org.dan.ping.pong.app.match.rule.service.common;

import static java.util.Comparator.comparing;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.WeightedMatches;
import static org.dan.ping.pong.util.collection.IterableComparator.LengthPolicy.LongerSmaller;

import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.group.HisIntPair;
import org.dan.ping.pong.app.match.rule.OrderRuleName;
import org.dan.ping.pong.app.match.rule.reason.WeightSetsReason;
import org.dan.ping.pong.util.collection.CmpValueCounter;
import org.dan.ping.pong.util.collection.IterableComparator;

import java.util.Collection;
import java.util.Comparator;
import java.util.function.Function;

public class WeightedMatchesRuleService extends AbstractWeightedMatchesRuleService {
    public static final Comparator<CmpValueCounter<HisIntPair>> CMP_VALUE_COUNTER_COMPARATOR
            = comparing(
            (Function<CmpValueCounter<HisIntPair>, HisIntPair>)
                    CmpValueCounter::getValue)
            .thenComparingInt(o -> -o.getRepeats());

    private static final IterableComparator<CmpValueCounter<HisIntPair>> WEIGHT_SET_CMP
            = new IterableComparator<>(CMP_VALUE_COUNTER_COMPARATOR);

    @Override
    public OrderRuleName getName() {
        return WeightedMatches;
    }

    @Override
    protected Comparator<CmpValueCounter<HisIntPair>> comparatorCmpValueCounter() {
        return CMP_VALUE_COUNTER_COMPARATOR;
    }

    private static class WeightSetsReasonComparator implements Comparator<WeightSetsReason> {
        @Override
        public int compare(WeightSetsReason o1, WeightSetsReason o2) {
            return WEIGHT_SET_CMP.compare(
                    o1.getWeightSets(), o2.getWeightSets(), LongerSmaller);
        }
    }

    private static final Comparator<WeightSetsReason> CMP = new WeightSetsReasonComparator();

    @Override
    protected Comparator<WeightSetsReason> comparatorWeightSetsReason() {
        return CMP;
    }
}
