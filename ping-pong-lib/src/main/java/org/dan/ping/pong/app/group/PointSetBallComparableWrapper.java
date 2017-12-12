package org.dan.ping.pong.app.group;

import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Builder
public class PointSetBallComparableWrapper {
    @Getter
    private final BidSuccessInGroup stat;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PointSetBallComparableWrapper that = (PointSetBallComparableWrapper) o;

        return stat.getPunkts() == that.stat.getPunkts()
                && stat.getWinSets() == that.stat.getWinSets()
                && stat.getLostSets() == that.stat.getLostSets()
                && stat.getWinBalls() == that.stat.getWinBalls()
                && stat.getLostBalls() == that.stat.getLostBalls();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(stat.getPunkts())
                .append(stat.getWinSets())
                .append(stat.getLostSets())
                .append(stat.getWinBalls())
                .append(stat.getLostBalls())
                .build();
    }
}
