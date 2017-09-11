package org.dan.ping.pong.app.tournament;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingByConcurrent;
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
import org.dan.ping.pong.app.match.Pid;
import org.dan.ping.pong.app.match.SetScoreResult;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Getter
@Setter
@Builder
public class OpenTournamentMemState {
    private int tid;
    private Pid pid;
    private Set<Integer> adminIds;
    private Map<Integer, ParticipantMemState> participants;
    private Map<Integer, MatchInfo> matches;
    private Map<Integer, GroupInfo> groups;
    private Map<Integer, CategoryInfo> categories;
    private TournamentRules rule;
    private TournamentState state;
    private Optional<Instant> completeAt;

    public MatchInfo getMatchById(int mid) {
        return ofNullable(matches.get(mid))
                .orElseThrow(() -> notFound("Match " + mid + " doesn't exist"));
    }

    public boolean isAdminOf(int uid) {
        return adminIds.contains(uid);
    }

    public ParticipantMemState getParticipant(int uid) {
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

    public ParticipantMemState getBid(int bid) {
        return participants.get(bid);
    }

    public List<GroupInfo> getGroupsByCategory(int cid) {
        return groups.values().stream()
                .filter(groupInfo -> groupInfo.getCid() == cid)
                .collect(toList());
    }

    public void checkAdmin(int uid) {
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
}
