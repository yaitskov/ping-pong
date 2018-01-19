package org.dan.ping.pong.app.match;

import static java.time.Duration.between;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toMap;
import static org.dan.ping.pong.app.tournament.ParticipantMemState.FILLER_LOSER_UID;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.tournament.Tid;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@Slf4j
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
    /**
     * a transient flag to cover a rare situation when both participants are fake uid = 1.
     * It could happen in a match for the 3rd place if 1 player
     * from every branch resigns or gets expelled.
     * */
    private boolean losersMeet;

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

    public void checkParticipantSpace() {
        if (getParticipantIdScore().size() == 2 || losersMeet) {
            throw internalError("Match " + mid + " gets 3rd participant");
        }
    }

    public int numberOfParticipants() {
        if (losersMeet) {
            return 2;
        }
        return participantIdScore.size();
    }

    public boolean addParticipant(Uid uid) {
        if (FILLER_LOSER_UID.equals(uid) && participantIdScore.containsKey(uid)) {
            losersMeet = true;
        } else {
            if (participantIdScore.containsKey(uid)) {
                log.warn("ReAdd uid {} to mid {}. Allowed for rescore", uid, mid);
                return true;
            }
            checkParticipantSpace();
            participantIdScore.put(uid, new ArrayList<>());
        }
        return false;
    }

    public void loadParticipants(Map<Uid, List<Integer>> scores) {
        if (scores.size() != 2) {
            throw internalError("Participants must be loaded in pairs");
        }
        losersMeet = false;
        participantIdScore = scores;
    }

    public List<Integer> getParticipantScore(Uid uid) {
        if (FILLER_LOSER_UID.equals(uid)) {
            return emptyList();
        }
        return participantIdScore.get(uid);
    }

    public boolean removeParticipant(Uid uid) {
        if (losersMeet) {
            if (FILLER_LOSER_UID.equals(uid)) {
                losersMeet = false;
                return true;
            }
            return false;
        } else {
            return participantIdScore.remove(uid) != null;
        }
    }

    public Stream<Uid> participants() {
        if (losersMeet) {
            return Stream.of(FILLER_LOSER_UID, FILLER_LOSER_UID);
        }
        return getParticipantIdScore().keySet().stream();
    }

    public Map<Uid, List<Integer>> sliceFirstSets(int setNumber) {
        return participantIdScore.entrySet().stream()
                .collect(toMap(Map.Entry::getKey,
                        e -> e.getValue().subList(0, setNumber)));
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

    public void addSetScore(List<IdentifiedScore> scores) {
        scores.forEach(score -> participantIdScore.get(score.getUid()).add(score.getScore()));
    }

    public Set<Uid> getUids() {
        return participantIdScore.keySet();
    }

    public Optional<Uid> leftUid() {
        return participantIdScore.keySet().stream().findAny();
    }

    public Optional<Uid> getOpponentUid(Uid uid) {
        if (FILLER_LOSER_UID.equals(uid) && losersMeet) {
            return Optional.of(uid);
        }
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
