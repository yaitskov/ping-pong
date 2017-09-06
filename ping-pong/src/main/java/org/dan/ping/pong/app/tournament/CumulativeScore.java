package org.dan.ping.pong.app.tournament;

import static java.util.Comparator.comparing;
import static org.dan.ping.pong.app.group.BidSuccessInGroup.BEST_COMPARATOR;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.dan.ping.pong.app.group.BidSuccessInGroup;

import java.util.Comparator;

@Getter
@Setter
@Builder
public class CumulativeScore {
    public static final Comparator<CumulativeScore> BEST_ORDER
            = comparing(CumulativeScore::getWeighted, BEST_COMPARATOR);

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
