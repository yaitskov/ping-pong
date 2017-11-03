package org.dan.ping.pong.app.group;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.app.bid.Uid;

@Setter
@Getter
@Builder
@AllArgsConstructor
@ToString(of={"uid","finalState","punkts"})
@NoArgsConstructor(onConstructor = @__(@JsonCreator))
public class BidSuccessInGroup {
    private Uid uid;
    private BidState finalState;
    private int punkts;
    private int winSets;
    private int lostSets;
    private int winBalls;
    private int lostBalls;

    public BidSuccessInGroup(Uid uid, BidState finalState) {
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
        checkArgument(uid.equals(b.uid));
        checkArgument(finalState == b.finalState);
        return new BidSuccessInGroup(uid, finalState, punkts + b.punkts,
                winSets + b.winSets,
                lostSets + b.lostSets,
                winBalls + b.winBalls,
                lostBalls + b.lostBalls);
    }
}
