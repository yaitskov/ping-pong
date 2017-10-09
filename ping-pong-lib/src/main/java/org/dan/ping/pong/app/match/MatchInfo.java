package org.dan.ping.pong.app.match;

import static java.time.Duration.between;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor(onConstructor = @__(@JsonCreator))
@AllArgsConstructor
public class MatchInfo {
    private int mid;
    private int tid;
    private int cid;
    private MatchType type;
    private Optional<Integer> gid;
    private MatchState state;
    private Optional<Integer> loserMid;
    private Optional<Integer> winnerMid;
    private Optional<Integer> winnerId;
    private Map<Integer, List<Integer>> participantIdScore;
    private Optional<Instant> startedAt;
    private Optional<Instant> endedAt;
    private int priority;

    public int getNumberOfSets() {
        for (List<Integer> l : participantIdScore.values()) {
            return l.size();
        }
        return 0;
    }

    public static class MatchInfoBuilder {
        Optional<Integer> gid = Optional.empty();
        Optional<Integer> loserMid = Optional.empty();
        Optional<Integer> winnerMid = Optional.empty();
        Optional<Integer> winnerId = Optional.empty();
        Optional<Instant> startedAt = Optional.empty();
        Optional<Instant> endedAt = Optional.empty();
    }

    public String toString() {
        return "Mid(" + mid + ")";
    }

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
        return rule.findWinnerId(rule.calcWonSets(getParticipantIdScore()));
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

    public Optional<Duration> duration(Instant now) {
        return startedAt.map(s -> endedAt.map(e -> between(s, e))
                .orElseGet(() -> between(s, now)));
    }
}
