package org.dan.ping.pong.app.group;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingInt;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.dan.ping.pong.app.bid.BidState;

import java.util.Comparator;

@Setter
@Getter
@Builder
@AllArgsConstructor
@ToString(of={"uid","finalState","punkts"})
@NoArgsConstructor(onConstructor = @__(@JsonCreator))
public class BidSuccessInGroup {
    public static final Comparator<BidSuccessInGroup> BEST_COMPARATOR
            = comparingInt((BidSuccessInGroup o) -> o.getFinalState().score())
            .thenComparing(Comparator.comparingInt(BidSuccessInGroup::getPunkts).reversed())
            .thenComparing(comparingInt(BidSuccessInGroup::getWinSets).reversed())
            .thenComparingInt(BidSuccessInGroup::getLostSets)
            .thenComparing(comparingInt(BidSuccessInGroup::getWinBalls).reversed())
            .thenComparingInt(BidSuccessInGroup::getLostBalls);

    private int uid;
    private BidState finalState;
    private int punkts;
    private int winSets;
    private int lostSets;
    private int winBalls;
    private int lostBalls;

    public BidSuccessInGroup(int uid, BidState finalState) {
        this.uid = uid;
        this.finalState = finalState;
    }

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

    public BidSuccessInGroup multiply(int k) {
        return new BidSuccessInGroup(uid, finalState, punkts * k,
                winSets * k, lostSets * k,
                winBalls * k, lostBalls * k);
    }

    public BidSuccessInGroup merge(BidSuccessInGroup b) {
        checkArgument(uid == b.uid);
        checkArgument(finalState == b.finalState);
        return new BidSuccessInGroup(uid, finalState, punkts + b.punkts,
                winSets + b.winSets,
                lostSets + b.lostSets,
                winBalls + b.winBalls,
                lostBalls + b.lostBalls);
    }
}
