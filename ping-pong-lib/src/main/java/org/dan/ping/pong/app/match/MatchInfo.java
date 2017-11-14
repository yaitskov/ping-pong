package org.dan.ping.pong.app.match;

import static java.time.Duration.between;
import static java.util.stream.Collectors.toMap;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.bid.Uid;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
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
    public static final String USER = "user";
    public static final String MATCH = "match";

    private Mid mid;
    private Tid tid;
    private int cid;
    private MatchType type;
    private Optional<Integer> gid;
    private MatchState state;
    private Optional<Mid> loserMid;
    private Optional<Mid> winnerMid;
    private Optional<Uid> winnerId;
    private Map<Uid, List<Integer>> participantIdScore;
    private Optional<Instant> startedAt;
    private Optional<Instant> endedAt;
    private int priority;
    private int level;

    public int getPlayedSets() {
        for (List<Integer> l : participantIdScore.values()) {
            return l.size();
        }
        return 0;
    }

    public void checkParticipant(Uid uid) {
        if (!participantIdScore.containsKey(uid)) {
            throw badRequest("user-not-plays-match",
                    ImmutableMap.of(USER, uid, MATCH, mid));
        }
    }

    public static class MatchInfoBuilder {
        Optional<Integer> gid = Optional.empty();
        Optional<Mid> loserMid = Optional.empty();
        Optional<Mid> winnerMid = Optional.empty();
        Optional<Uid> winnerId = Optional.empty();
        Optional<Instant> startedAt = Optional.empty();
        Optional<Instant> endedAt = Optional.empty();
    }

    public String toString() {
        return "Mid(" + mid + ")";
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

    public MatchInfo clone() {
        return MatchInfo.builder()
                .tid(tid)
                .mid(mid)
                .level(level)
                .endedAt(endedAt)
                .cid(cid)
                .state(state)
                .loserMid(loserMid)
                .winnerMid(winnerMid)
                .winnerId(winnerId)
                .type(type)
                .priority(priority)
                .gid(gid)
                .participantIdScore(participantIdScore.entrySet()
                        .stream().collect(toMap(Map.Entry::getKey,
                                e -> new ArrayList<>(e.getValue()))))
                .build();
    }
}
