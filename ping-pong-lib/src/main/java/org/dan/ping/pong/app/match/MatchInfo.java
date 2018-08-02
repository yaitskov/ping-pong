package org.dan.ping.pong.app.match;

import static java.time.Duration.between;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;
import static org.dan.ping.pong.app.match.MatchTag.DISAMBIGUATION;
import static org.dan.ping.pong.app.match.TidMid.tidMidOf;
import static org.dan.ping.pong.app.match.dispute.MatchSets.ofSets;
import static org.dan.ping.pong.app.tournament.ParticipantMemState.BID;
import static org.dan.ping.pong.app.tournament.ParticipantMemState.FILLER_LOSER_BID;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.ImmutableMap;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.category.Cid;
import org.dan.ping.pong.app.group.Gid;
import org.dan.ping.pong.app.match.dispute.MatchSets;
import org.dan.ping.pong.app.playoff.RootTaggedMatch;
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
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MatchInfo {
    public static final String USER = "user";
    public static final String MATCH = "match";
    public static final String MID = "mid";

    private Mid mid;
    private Tid tid;
    private Cid cid;
    private MatchType type;
    private Optional<MatchTag> tag = Optional.empty();
    private Optional<Gid> gid = Optional.empty();
    private MatchState state;
    private Optional<Mid> loserMid = Optional.empty();
    private Optional<Mid> winnerMid = Optional.empty();
    private Optional<Bid> winnerId = Optional.empty();
    @JsonInclude // export to json removes all keys
    private Map<Bid, List<Integer>> participantIdScore = emptyMap();
    private Optional<Instant> startedAt = Optional.empty();
    private Optional<Instant> endedAt = Optional.empty();
    private int priority;
    private int level;
    /**
     * a transient flag to cover a rare situation when both participants are fake uid = 1.
     * It could happen in a match for the 3rd place if 1 player
     * from every branch resigns or gets expelled.
     * */
    private boolean losersMeet;

    public Bid winnerId() {
        return winnerId.orElseThrow(() -> internalError("no winner in match", MID, mid));
    }

    public int playedSets() {
        for (List<Integer> l : participantIdScore.values()) {
            return l.size();
        }
        return 0;
    }

    public void checkParticipant(Bid bid) {
        if (!participantIdScore.containsKey(bid)) {
            throw badRequest("user-not-plays-match",
                    ImmutableMap.of(USER, bid, MATCH, mid));
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

    public boolean addParticipant(Bid bid) {
        if (FILLER_LOSER_BID.equals(bid) && participantIdScore.containsKey(bid)) {
            losersMeet = true;
        } else {
            if (participantIdScore.containsKey(bid)) {
                log.warn("ReAdd uid {} to mid {}. Allowed for rescore", bid, mid);
                return true;
            }
            checkParticipantSpace();
            participantIdScore.put(bid, new ArrayList<>());
        }
        return false;
    }

    public void loadParticipants(MatchSets scores) {
        scores.validateNumberParticipants();
        losersMeet = false;
        participantIdScore = scores.getSets();
    }

    public List<Integer> getParticipantScore(Bid bid) {
        if (FILLER_LOSER_BID.equals(bid)) {
            return emptyList();
        }
        return participantIdScore.get(bid);
    }

    public boolean removeParticipant(Bid bid) {
        if (losersMeet) {
            if (FILLER_LOSER_BID.equals(bid)) {
                losersMeet = false;
                return true;
            }
            return false;
        } else {
            return participantIdScore.remove(bid) != null;
        }
    }

    public Stream<Bid> participants() {
        if (losersMeet) {
            return Stream.of(FILLER_LOSER_BID, FILLER_LOSER_BID);
        }
        return getParticipantIdScore().keySet().stream();
    }

    public MatchSets sliceFirstSets(int setNumber) {
        return ofSets(participantIdScore.entrySet().stream()
                .collect(toMap(Map.Entry::getKey,
                        e -> e.getValue().subList(0, setNumber))));
    }

    public RootTaggedMatch toRootTaggedMatch() {
        return RootTaggedMatch.builder()
                .mid(mid)
                .tag(tag)
                .level(level)
                .build();
    }

    public boolean disambiguationP() {
        return tag.map(t -> t.getPrefix().equals(DISAMBIGUATION)).orElse(false);
    }

    public boolean inGroup() {
        return gid.isPresent();
    }

    public Gid groupId() {
        return gid.orElseThrow(() -> internalError("Match is not in a group", MID, mid));
    }

    @JsonIgnore
    public boolean isOver() {
        return winnerId.isPresent();
    }

    public static class MatchInfoBuilder {
        Optional<Gid> gid = Optional.empty();
        Optional<Mid> loserMid = Optional.empty();
        Optional<Mid> winnerMid = Optional.empty();
        Optional<Bid> winnerId = Optional.empty();
        Optional<Instant> startedAt = Optional.empty();
        Optional<Instant> endedAt = Optional.empty();
    }

    public String toString() {
        return "Mid(" + mid + ")";
    }

    public Map<Bid, Integer> getSetScore(int setOrdNumber) {
        final Map<Bid, Integer> setScore = new HashMap<>();
        participantIdScore.forEach(
                (bid, sets) -> setScore.put(bid, sets.get(setOrdNumber)));
        return setScore;
    }

    public void addSetScore(List<IdentifiedScore> scores) {
        scores.forEach(score -> participantIdScore.get(score.getBid()).add(score.getScore()));
    }

    public Set<Bid> bids() {
        return participantIdScore.keySet();
    }

    public Bid[] bidsArray() {
        return bids().toArray(new Bid[2]);
    }

    public Optional<Bid> leftBid() {
        return participantIdScore.keySet().stream().findAny();
    }

    public Bid opponentBid(Bid bid) {
        return getOpponentBid(bid)
                .orElseThrow(() -> internalError(
                        "Bid is not participant of match",
                        ImmutableMap.of(BID, bid, MID, mid)));
    }

    public Optional<Bid> loserBid() {
        return winnerId.flatMap(this::getOpponentBid);
    }

    public Optional<Bid> getOpponentBid(Bid bid) {
        if (FILLER_LOSER_BID.equals(bid) && losersMeet) {
            return Optional.of(bid);
        }
        return participantIdScore.keySet().stream()
                .filter(participantUid -> !participantUid.equals(bid))
                .findFirst();
    }

    public boolean hasParticipant(Bid bid) {
        return participantIdScore.containsKey(bid);
    }

    public Optional<Duration> duration(Instant now) {
        return startedAt.map(s -> endedAt.map(e -> between(s, e))
                .orElseGet(() -> between(s, now)));
    }

    public MatchInfo clone() {
        return MatchInfo.builder()
                .tid(tid)
                .tag(tag)
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

    boolean continues() {
        return !winnerId.isPresent();
    }

    public TidMid toTidMid() {
        return tidMidOf(tid, mid);
    }
}
