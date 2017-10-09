package org.dan.ping.pong.app.group;

import static java.util.Comparator.comparingInt;

import java.util.Comparator;

public enum DisambiguationPolicy {
    CMP_WIN_AND_LOSE {
        @Override
        public Comparator<BidSuccessInGroup> getComparator() {
            return WIN_AND_LOSE_COMPARATOR;
        }
    },
    CMP_WIN_MINUS_LOSE {
        @Override
        public Comparator<BidSuccessInGroup> getComparator() {
            return WIN_MINUS_LOSE_COMPARATOR;
        }
    };

    public static final Comparator<BidSuccessInGroup> WIN_AND_LOSE_COMPARATOR
            = comparingInt((BidSuccessInGroup o) -> o.getFinalState().score())
            .thenComparing(Comparator.comparingInt(BidSuccessInGroup::getPunkts).reversed())
            .thenComparing(comparingInt(BidSuccessInGroup::getWinSets).reversed())
            .thenComparingInt(BidSuccessInGroup::getLostSets)
            .thenComparing(comparingInt(BidSuccessInGroup::getWinBalls).reversed())
            .thenComparingInt(BidSuccessInGroup::getLostBalls);

    public static final Comparator<BidSuccessInGroup> WIN_MINUS_LOSE_COMPARATOR
            = comparingInt((BidSuccessInGroup o) -> o.getFinalState().score())
            .thenComparing(Comparator.comparingInt(BidSuccessInGroup::getPunkts).reversed())
            .thenComparing(comparingInt(BidSuccessInGroup::getWinSets).reversed())
            .thenComparingInt(BidSuccessInGroup::getLostSets)
            .thenComparing(comparingInt(
                    (BidSuccessInGroup bidSuccess) ->
                            bidSuccess.getWinBalls() - bidSuccess.getLostBalls())
                    .reversed());

    public abstract Comparator<BidSuccessInGroup> getComparator();
}
