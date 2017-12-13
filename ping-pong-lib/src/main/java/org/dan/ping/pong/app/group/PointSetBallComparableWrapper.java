package org.dan.ping.pong.app.group;

import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Builder
public class PointSetBallComparableWrapper {
    @Getter
    private final BidSuccessInGroup stat;
    private DisambiguationPolicy disambiguationPolicy;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PointSetBallComparableWrapper that = (PointSetBallComparableWrapper) o;

        switch (disambiguationPolicy) {
            case CMP_WIN_MINUS_LOSE:
                return stat.getPunkts() == that.stat.getPunkts()
                        && setBalance() == that.setBalance()
                        && ballBalance() == that.ballBalance();
            case CMP_WIN_AND_LOSE:
                return stat.getWinSets() == that.stat.getWinSets()
                        && stat.getLostSets() == that.stat.getLostSets()
                        && stat.getWinBalls() == that.stat.getWinBalls()
                        && stat.getLostBalls() == that.stat.getLostBalls();
            default:
                throw internalError("not implemented " + disambiguationPolicy);
        }
    }

    private int setBalance() {
        return stat.getWinSets() - stat.getLostSets();
    }

    @Override
    public int hashCode() {
        switch (disambiguationPolicy) {
            case CMP_WIN_MINUS_LOSE:
                return new HashCodeBuilder()
                        .append(stat.getPunkts())
                        .append(setBalance())
                        .append(ballBalance())
                        .build();

            case CMP_WIN_AND_LOSE:
                return new HashCodeBuilder()
                        .append(stat.getPunkts())
                        .append(stat.getWinSets())
                        .append(stat.getLostSets())
                        .append(stat.getWinBalls())
                        .append(stat.getLostBalls())
                        .build();
            default:
                throw internalError("not implemented " + disambiguationPolicy);
        }
    }

    private int ballBalance() {
        return stat.getWinBalls() - stat.getLostBalls();
    }
}
