package org.dan.ping.pong.app.playoff;

import static java.lang.Math.max;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.CumDiffBalls;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.CumDiffSets;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.Level;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.LostMatches;
import static org.dan.ping.pong.app.match.rule.reason.DecreasingIntScalarReason.ofIntD;
import static org.dan.ping.pong.app.match.rule.reason.DecreasingLongScalarReason.ofLongD;
import static org.dan.ping.pong.app.match.rule.reason.IncreasingIntScalarReason.ofIntI;

import lombok.Getter;
import lombok.Setter;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.match.rule.reason.Reason;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Getter
@Setter
public class PlayOffBidStat {
    public static final Comparator<PlayOffBidStat> PLAY_OFF_BID_STAT_COMPARATOR
            = comparingInt((PlayOffBidStat s) -> -s.getHighestLevel())
            .thenComparingInt(s -> s.getLostMatches())
            .thenComparingLong(s -> -s.getCumulativeSetsBalance())
            .thenComparingLong(s -> -s.getCumulativeBallsBalance());

    private Bid bid;
    private int highestLevel;
    private int lostMatches;
    private long cumulativeSetsBalance;
    private long cumulativeBallsBalance;

    public PlayOffBidStat(Bid bid) {
        this.bid = bid;
    }

    public void incLost() {
        ++lostMatches;
    }

    public void maxLevel(int level) {
        highestLevel = max(highestLevel, level);
    }

    public void addSetsBalance(long setBalance) {
        cumulativeSetsBalance += setBalance;
    }

    public void addBallsBalance(long ballBalance) {
        cumulativeBallsBalance += ballBalance;
    }

    public List<Optional<Reason>> toReasonChain() {
        return Stream.of(
                ofIntD(bid, highestLevel, Level),
                ofIntI(bid, lostMatches, LostMatches),
                ofLongD(bid, cumulativeSetsBalance, CumDiffSets),
                ofLongD(bid, cumulativeBallsBalance, CumDiffBalls))
                .map(Optional::of)
                .collect(toList());
    }
}
