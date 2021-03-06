package org.dan.ping.pong.app.match;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.tournament.Tid;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RescoreMatch {
    private Tid tid;
    private Mid mid;
    private Optional<String> effectHash = Optional.empty();
    private Map<Bid, List<Integer>> sets;

    public static class RescoreMatchBuilder {
        Optional<String> effectHash = Optional.empty();
    }
}
