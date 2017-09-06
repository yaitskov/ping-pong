package org.dan.ping.pong.app.match;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dan.ping.pong.app.tournament.MatchValidationRule;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchInfo {
    private int mid;
    private int tid;
    private int cid;
    private MatchType type;
    private Optional<Integer> gid = Optional.empty();
    private MatchState state;
    private Optional<Integer> loserMid = Optional.empty();
    private Optional<Integer> winnerMid = Optional.empty();
    private Optional<Integer> winnerId = Optional.empty();
    private Map<Integer, List<Integer>> participantIdScore;
    private Optional<Instant> startedAt;

    public int getPlayedSets() {
        for (List<Integer> sets : participantIdScore.values()) {
            return sets.size();
        }
        return 0;
    }

    public Map<Integer, Integer> getSetScore(int setOrdNumber) {
        final Map<Integer, Integer> setScore = new HashMap<>();
        participantIdScore.forEach(
                (uid, sets) -> setScore.put(uid, sets.get(setOrdNumber)));
        return setScore;
    }

    public Optional<Integer> addSetScore(List<IdentifiedScore> scores, MatchValidationRule rule) {
        scores.forEach(score -> participantIdScore.get(score.getUid()).add(score.getScore()));
        return rule.findWinnerId(rule.calcWonSets(this));
    }

    public Set<Integer> getUids() {
        return participantIdScore.keySet();
    }

    public Optional<Integer> getOpponentUid(int uid) {
        return participantIdScore.keySet().stream()
                .filter(participantUid -> participantUid != uid)
                .findFirst();
    }

    public boolean hasParticipant(int uid) {
        return participantIdScore.containsKey(uid);
    }
}
