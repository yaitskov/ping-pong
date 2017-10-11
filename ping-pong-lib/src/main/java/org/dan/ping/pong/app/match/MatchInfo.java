package org.dan.ping.pong.app.match;

import static java.time.Duration.between;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dan.ping.pong.app.tournament.Uid;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.swing.UIDefaults;

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
    private Optional<Uid> winnerId;
    private Map<Uid, List<Integer>> participantIdScore;
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
        Optional<Uid> winnerId = Optional.empty();
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

    public Map<Uid, Integer> getSetScore(int setOrdNumber) {
        final Map<Uid, Integer> setScore = new HashMap<>();
        participantIdScore.forEach(
                (uid, sets) -> setScore.put(uid, sets.get(setOrdNumber)));
        return setScore;
    }

    public Optional<Uid> addSetScore(List<IdentifiedScore> scores, MatchValidationRule rule) {
        scores.forEach(score -> participantIdScore.get(score.getUid()).add(score.getScore()));
        return rule.findWinnerId(rule.calcWonSets(getParticipantIdScore()));
    }

    public Set<Uid> getUids() {
        return participantIdScore.keySet();
    }

    public Optional<Uid> getOpponentUid(Uid uid) {
        return participantIdScore.keySet().stream()
                .filter(participantUid -> !participantUid.equals(uid))
                .findFirst();
    }

    public boolean hasParticipant(Uid uid) {
        return participantIdScore.containsKey(uid);
    }

    public Optional<Duration> duration(Instant now) {
        return startedAt.map(s -> endedAt.map(e -> between(s, e))
                .orElseGet(() -> between(s, now)));
    }
}
