package org.dan.ping.pong.app.bid.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Optional;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BidTimeStats {
    private long avgMs;
    private long totalMs;
    private Instant enlistedAt;
    private Optional<Instant> completeAt = Optional.empty();

    public static class BidTimeStatsBuilder {
        Optional<Instant> completeAt = Optional.empty();
    }
}
