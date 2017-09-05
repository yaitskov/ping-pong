package org.dan.ping.pong.app.group;

import static java.util.Comparator.comparingInt;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Comparator;

@Setter
@Getter
@RequiredArgsConstructor
public class BidSuccessInGroup {
    public static final Comparator<BidSuccessInGroup> BEST_COMPARATOR
            = comparingInt(BidSuccessInGroup::getPunkts).reversed()
            .thenComparing(comparingInt(BidSuccessInGroup::getWinSets).reversed())
            .thenComparingInt(BidSuccessInGroup::getLostSets)
            .thenComparing(comparingInt(BidSuccessInGroup::getWinBalls).reversed())
            .thenComparingInt(BidSuccessInGroup::getLostBalls);

    private final int uid;
    private int punkts;
    private int winSets;
    private int lostSets;
    private int winBalls;
    private int lostBalls;

    public void win() {
        punkts += 2;
    }

    public void lost() {
        punkts += 1;
    }

    public void winBalls(int balls) {
        winBalls += balls;
    }

    public void lostBalls(int balls) {
        lostBalls += balls;
    }

    public void wonSets(Integer sets) {
        winSets += sets;
    }

    public void lostSets(Integer sets) {
        lostSets += sets;
    }
}
