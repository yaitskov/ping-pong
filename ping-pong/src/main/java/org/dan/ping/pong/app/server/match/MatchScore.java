package org.dan.ping.pong.app.server.match;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchScore {
    private int tid;
    private int mid;
    private Optional<Integer> winUid;
    private Map<Integer, Integer> wonSets;
    private Map<Integer, List<Integer>> sets;

    public static class MatchScoreBuilder {
        private Optional<Integer> winUid = Optional.empty();
    }
}
