package org.dan.ping.pong.app.tournament;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;
import static org.dan.ping.pong.sys.error.PiPoEx.forbidden;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;
import static org.dan.ping.pong.sys.error.PiPoEx.notFound;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.dan.ping.pong.app.category.CategoryInfo;
import org.dan.ping.pong.app.group.GroupInfo;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.place.Pid;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@Getter
@Setter
@Builder
public class OpenTournamentMemState {
    private Tid tid;
    private String name;
    private Pid pid;
    private Set<Uid> adminIds;
    private Map<Uid, ParticipantMemState> participants;
    private Map<Integer, MatchInfo> matches;
    private Map<Integer, GroupInfo> groups;
    private Map<Integer, CategoryInfo> categories;
    private TournamentRules rule;
    private TournamentState state;
    private Optional<Instant> completeAt;
    private Optional<Double> ticketPrice;
    private Optional<Tid> previousTid;
    private Instant opensAt;

    public Optional<MatchInfo> maybeMatchById(int mid) {
        return ofNullable(matches.get(mid));
    }

    public MatchInfo getMatchById(int mid) {
        return maybeMatchById(mid)
                .orElseThrow(() -> badRequest("match-not-in-tournament", "mid", mid));
    }

    public boolean isAdminOf(Uid uid) {
        return adminIds.contains(uid);
    }

    public ParticipantMemState getParticipant(Uid uid) {
        return ofNullable(participants.get(uid))
                .orElseThrow(() -> notFound("User " + uid
                        + " does participate in the tournament " + tid));
    }

    public SetScoreResultName matchScoreResult() {
        if (state == TournamentState.Open) {
            return SetScoreResultName.MatchComplete;
        } else if (state == TournamentState.Close) {
            return SetScoreResultName.LastMatchComplete;
        } else {
            throw internalError("Unexpected tournament state " + state);
        }
    }

    public ParticipantMemState getBid(Uid bid) {
        return participants.get(bid);
    }

    public List<GroupInfo> getGroupsByCategory(int cid) {
        return groups.values().stream()
                .filter(groupInfo -> groupInfo.getCid() == cid)
                .collect(toList());
    }

    public void checkAdmin(Uid uid) {
        if (isAdminOf(uid)) {
            return;
        }
        throw forbidden("You (" + uid + ") are not an administrator of " + tid);
    }

    public CategoryInfo getCategory(int cid) {
        return categories.get(cid);
    }

    public void checkCategory(int cid) {
        if (categories.containsKey(cid)) {
            return;
        }
        throw badRequest("Category " + cid + " is not in tournament " + tid);
    }

    public TournamentLink toLink() {
        return TournamentLink.builder()
                .name(name)
                .tid(tid)
                .build();
    }

    public Stream<MatchInfo> participantMatches(Uid uid) {
        return matches.values().stream()
                .filter(m -> m.getParticipantIdScore().containsKey(uid));
    }
}
