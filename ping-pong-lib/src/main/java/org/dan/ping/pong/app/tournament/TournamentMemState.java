package org.dan.ping.pong.app.tournament;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.dan.ping.pong.app.bid.BidState.Expl;
import static org.dan.ping.pong.app.bid.BidState.Quit;
import static org.dan.ping.pong.app.tournament.ParticipantMemState.FILLER_LOSER_UID;
import static org.dan.ping.pong.app.tournament.ParticipantMemState.createLoserBid;
import static org.dan.ping.pong.app.user.UserRole.Admin;
import static org.dan.ping.pong.app.user.UserRole.Spectator;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;
import static org.dan.ping.pong.sys.error.PiPoEx.forbidden;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;
import static org.dan.ping.pong.sys.error.PiPoEx.notFound;

import com.google.common.collect.ImmutableMap;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.category.CategoryLink;
import org.dan.ping.pong.app.group.GroupInfo;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.Mid;
import org.dan.ping.pong.app.match.dispute.DisputeMemState;
import org.dan.ping.pong.app.match.rule.rules.GroupOrderRule;
import org.dan.ping.pong.app.place.Pid;
import org.dan.ping.pong.app.playoff.PowerRange;
import org.dan.ping.pong.app.sport.SportType;
import org.dan.ping.pong.app.user.UserRole;
import org.dan.ping.pong.sys.error.PiPoEx;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@Getter
@Setter
@Builder
public class TournamentMemState {
    public static final String EXPECTED_TOURNAMENT_STATE = "expected_state";
    public static final String TOURNAMENT_STATE = "state";
    public static final String TID = "tid";

    private SportType sport;
    private Tid tid;
    private String name;
    private TournamentType type;
    private Pid pid;
    private Set<Uid> adminIds;
    private Map<Uid, ParticipantMemState> participants;
    private Map<Mid, MatchInfo> matches;
    private Map<Integer, GroupInfo> groups;
    private Map<Integer, CategoryLink> categories;
    private TournamentRules rule;
    private TournamentState state;
    private Optional<Instant> completeAt;
    private Optional<Double> ticketPrice;
    private Optional<Tid> previousTid;
    private Instant opensAt;
    private List<DisputeMemState> disputes;
    private OneTimeCondActions condActions;
    private PowerRange powerRange;

    public Optional<MatchInfo> maybeMatchById(Mid mid) {
        return ofNullable(matches.get(mid));
    }

    public MatchInfo getMatchById(Mid mid) {
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

    public ParticipantMemState getBidOrQuit(Uid uid) {
        return getBidOr(uid,  Quit);
    }

    public ParticipantMemState getBidOrExpl(Uid uid) {
        return getBidOr(uid,  Expl);
    }

    public ParticipantMemState getBidOr(Uid bid, BidState state) {
        final ParticipantMemState result = participants.get(bid);
        if (result == null) {
            if (FILLER_LOSER_UID.equals(bid)) {
                return createLoserBid(tid, -1, state);
            }
            throw internalError("User " + bid
                    + " does participate in the tournament " + tid);
        }
        return result;
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

    public CategoryLink getCategory(int cid) {
        return ofNullable(categories.get(cid))
                .orElseThrow(() -> internalError("No category " + cid + " in tid " + tid));
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

    public void checkState(TournamentState expectedState) {
        if (state != expectedState) {
            throw PiPoEx.badRequest("tournament state is not but",
                    ImmutableMap.of(
                            TOURNAMENT_STATE, state,
                            EXPECTED_TOURNAMENT_STATE, expectedState));
        }
    }

    public UserRole detectRole(Optional<Uid> ouid) {
        return ouid.map(uid -> isAdminOf(uid) ? Admin : Spectator).orElse(Spectator);
    }

    public RewardRules rewards() {
        return getRule().getRewards().orElse(RewardRules.defaultRewards);
    }

    public Optional<Integer> findCidByName(String name) {
        return categories.values().stream()
                .filter(category -> category.getName().equals(name))
                .findAny()
                .map(CategoryLink::getCid);
    }

    public GroupInfo getGroup(int gid) {
        return ofNullable(groups.get(gid))
                .orElseThrow(() -> internalError("Tournament " + tid + " has no group " + gid));
    }

    public boolean disambiguationMatchNotPossible() {
        return !rule.getGroup()
                .orElseThrow(() -> internalError("Tournament has no groups", TID, tid))
                .getDisambiguationMatch().isPresent();
    }

    public List<GroupOrderRule> orderRules() {
        return rule.group().getOrderRules();
    }

    public List<ParticipantMemState> findBidsByCategory(int cid) {
        return participants.values().stream()
                .filter(bid -> bid.getCid() == cid)
                .collect(toList());
    }

    public List<MatchInfo> findGroupMatchesByCategory(int cid) {
        return matches.values().stream().filter(m -> m.getCid() == cid)
                .collect(toList());
    }

    public Set<Uid> uidsInGroup(int gid) {
        final Optional<Integer> ogid = Optional.of(gid);
        return participants.values().stream().filter(p -> p.getGid().equals(ogid))
                .map(ParticipantMemState::getUid)
                .collect(toSet());
    }

    public Set<Uid> uidsInCategory(int cid) {
        return participants.values().stream().filter(p -> p.getCid() == cid)
                .map(ParticipantMemState::getUid)
                .collect(toSet());
    }
}
