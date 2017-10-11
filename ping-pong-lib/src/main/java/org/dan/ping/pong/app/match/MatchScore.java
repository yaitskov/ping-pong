package org.dan.ping.pong.app.match;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dan.ping.pong.app.tournament.Uid;

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
    private Optional<Uid> winUid;
    private Map<Uid, Integer> wonSets;
    private Map<Uid, List<Integer>> sets;

    public static class MatchScoreBuilder {
        private Optional<Uid> winUid = Optional.empty();
    }
}
