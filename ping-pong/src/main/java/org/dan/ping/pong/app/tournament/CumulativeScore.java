package org.dan.ping.pong.app.tournament;

import static java.util.Comparator.comparing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dan.ping.pong.app.group.BidSuccessInGroup;

import java.util.Comparator;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CumulativeScore {
    public static Comparator<CumulativeScore> createComparator(
            Comparator<BidSuccessInGroup> bidSuccessComparator) {
        return comparing(CumulativeScore::getWeighted, bidSuccessComparator);
    }

    private BidSuccessInGroup rating;
    private BidSuccessInGroup weighted;
    private int level;

    public CumulativeScore merge(CumulativeScore b) {
        return CumulativeScore.builder()
                .level(Math.max(level, b.level))
                .rating(rating.merge(b.getRating()))
                .weighted(weighted.merge(b.getWeighted()))
                .build();
    }
}
