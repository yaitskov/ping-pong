package org.dan.ping.pong.app.tournament;

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.dan.ping.pong.app.bid.BidState.Expl;
import static org.dan.ping.pong.app.bid.BidState.Here;
import static org.dan.ping.pong.app.bid.BidState.Lost;
import static org.dan.ping.pong.app.bid.BidState.Paid;
import static org.dan.ping.pong.app.bid.BidState.Play;
import static org.dan.ping.pong.app.bid.BidState.Quit;
import static org.dan.ping.pong.app.bid.BidState.Wait;
import static org.dan.ping.pong.app.bid.BidState.Want;
import static org.dan.ping.pong.app.bid.BidState.Win1;
import static org.dan.ping.pong.app.bid.BidState.Win2;
import static org.dan.ping.pong.app.bid.BidState.Win3;
import static org.dan.ping.pong.app.group.GroupService.DM_TAG;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.UseDisambiguationMatches;
import static org.dan.ping.pong.app.match.rule.OrderRuleName._DisambiguationPreview;
import static org.dan.ping.pong.app.tournament.ParticipantMemState.FILLER_LOSER_BID;
import static org.dan.ping.pong.app.tournament.ParticipantMemState.createLoserBid;
import static org.dan.ping.pong.app.user.UserRole.Admin;
import static org.dan.ping.pong.app.user.UserRole.Spectator;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;
import static org.dan.ping.pong.sys.error.PiPoEx.forbidden;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;
import static org.dan.ping.pong.sys.error.PiPoEx.notFound;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.castinglots.rank.CastingLotsRule;
import org.dan.ping.pong.app.category.CategoryLink;
import org.dan.ping.pong.app.category.CategoryMemState;
import org.dan.ping.pong.app.category.Cid;
import org.dan.ping.pong.app.group.Gid;
import org.dan.ping.pong.app.group.GroupInfo;
import org.dan.ping.pong.app.group.GroupRules;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.Mid;
import org.dan.ping.pong.app.match.dispute.DisputeMemState;
import org.dan.ping.pong.app.match.rule.OrderRuleName;
import org.dan.ping.pong.app.match.rule.rules.GroupOrderRule;
import org.dan.ping.pong.app.match.rule.rules.meta.UseDisambiguationMatchesDirective;
import org.dan.ping.pong.app.place.Pid;
import org.dan.ping.pong.app.playoff.PlayOffRule;
import org.dan.ping.pong.app.playoff.PowerRange;
import org.dan.ping.pong.app.sport.MatchRules;
import org.dan.ping.pong.app.sport.SportType;
import org.dan.ping.pong.app.user.UserRole;
import org.dan.ping.pong.sys.error.PiPoEx;
import org.dan.ping.pong.util.counter.BidSeqGen;
import org.dan.ping.pong.util.counter.CidSeqGen;
import org.dan.ping.pong.util.counter.DidSeqGen;
import org.dan.ping.pong.util.counter.GidSeqGen;
import org.dan.ping.pong.util.counter.MidSeqGen;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
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
    public static final String PLACE_IS_BUSY = "place-is-busy";

    private SportType sport;
    private Tid tid;
    private String name;
    private TournamentType type;
    private Pid pid;
    private Set<Uid> adminIds;
    private Map<Bid, ParticipantMemState> participants;
    private Map<Uid, Map<Cid, Bid>> uidCid2Bid;
    private Map<Mid, MatchInfo> matches;
    private Map<Gid, GroupInfo> groups;
    private Map<Cid, CategoryMemState> categories;
    private TournamentRules rule;
    private TournamentState state;
    private Optional<Instant> completeAt;
    private Optional<Double> ticketPrice;
    private Optional<Tid> previousTid;
    private Instant opensAt;
    private List<DisputeMemState> disputes;
    private OneTimeCondActions condActions;
    private PowerRange powerRange;
    private CidSeqGen nextCategory;
    private DidSeqGen nextDispute;
    private GidSeqGen nextGroup;
    private MidSeqGen nextMatch;
    private BidSeqGen nextBid;

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

    public ParticipantMemState getParticipant(Bid bid) {
        return ofNullable(participants.get(bid))
                .orElseThrow(() -> notFound("Participant " + bid
                        + " does not participate in the tournament " + tid));
    }

    public List<CategoryLink> categoryLinks() {
        return categories.values()
                .stream()
                .map(CategoryMemState::toLink)
                .collect(toList());
    }

    public Stream<ParticipantMemState> participants() {
        return participants.values().stream();
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

    public ParticipantMemState getBid(Bid bid) {
        return participants.get(bid);
    }

    public ParticipantMemState getBidOrQuit(Bid uid) {
        return getBidOr(uid,  Quit);
    }

    public ParticipantMemState getBidOrExpl(Bid uid) {
        return getBidOr(uid,  Expl);
    }

    public ParticipantMemState getBidOr(Bid bid, BidState state) {
        final ParticipantMemState result = participants.get(bid);
        if (result == null) {
            if (FILLER_LOSER_BID.equals(bid)) {
                return createLoserBid(tid, new Cid(-1), state);
            }
            throw internalError("User " + bid
                    + " does participate in the tournament " + tid);
        }
        return result;
    }

    public List<GroupInfo> getGroupsByCategory(Cid cid) {
        return groups.values().stream()
                .filter(groupInfo -> groupInfo.getCid().equals(cid))
                .collect(toList());
    }

    public void checkAdmin(Uid uid) {
        if (isAdminOf(uid)) {
            return;
        }
        throw forbidden("You (" + uid + ") are not an administrator of " + tid);
    }

    public CategoryMemState getCategory(Cid cid) {
        return ofNullable(categories.get(cid))
                .orElseThrow(() -> internalError("No category " + cid + " in tid " + tid));
    }

    public void checkCategory(Cid cid) {
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

    public Stream<MatchInfo> matches() {
        return matches.values().stream();
    }

    public Stream<MatchInfo> findMatchesByCid(Cid cid) {
        return matches().filter(m -> m.getCid().equals(cid));
    }

    public Stream<MatchInfo> participantMatches(Bid bid) {
        return matches.values().stream()
                .filter(m -> m.getParticipantIdScore().containsKey(bid));
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

    public Cid findCidByName(CategoryMemState state) {
        return findCidByNameO(state.getName()).orElseThrow(
                () -> internalError("tournament " + tid + " has no category ["
                        + state.getName()));
    }

    public Optional<Cid> findCidByNameO(String name) {
        return categories.values().stream()
                .filter(category -> category.getName().equals(name))
                .findAny()
                .map(CategoryMemState::getCid);
    }

    public GroupInfo getGroup(Gid gid) {
        return ofNullable(groups.get(gid))
                .orElseThrow(() -> internalError("Tournament " + tid + " has no group " + gid));
    }

    private static final Set<OrderRuleName> DIS_MOD_RULES = ImmutableSet.of(
            UseDisambiguationMatches, _DisambiguationPreview);

    public Optional<MatchRules> disambiguationMatchRules() {
        return rule.getGroup()
                .flatMap(g -> g.getOrderRules()
                        .stream()
                        .filter(gor -> UseDisambiguationMatches.equals(gor.name()))
                        .findAny()
                        .map(UseDisambiguationMatchesDirective.class::cast)
                        .flatMap(UseDisambiguationMatchesDirective::getMatchRules));
    }

    public boolean disambiguationMatchNotPossible() {
        return !rule.getGroup()
                .map(g -> g.getOrderRules()
                        .stream()
                        .map(GroupOrderRule::name)
                        .filter(DIS_MOD_RULES::contains)
                        .findAny()
                        .map(x -> true)
                        .orElse(false))
                .orElse(false);
    }

    public List<GroupOrderRule> orderRules() {
        return rule.group(tid).getOrderRules();
    }

    public GroupRules groupRules() {
        return rule.group(tid);
    }

    public PlayOffRule playOffRules() {
        return rule.playOff(tid);
    }

    public List<MatchInfo> findGroupMatchesByCategory(Cid cid) {
        return matches.values().stream()
                .filter(m -> m.getCid().equals(cid) && m.getGid().isPresent())
                .collect(toList());
    }

    public Set<Bid> bidsInGroup(Gid gid) {
        return groupBids(of(gid)).map(ParticipantMemState::getBid).collect(toSet());
    }

    public Set<Bid> bidsInCategory(Cid cid) {
        return participants.values().stream().filter(p -> p.getCid().equals(cid))
                .map(ParticipantMemState::getBid)
                .collect(toSet());
    }

    public MatchRules selectMatchRule(MatchInfo match) {
        if (match.getGid().isPresent()) {
            return match
                    .getTag()
                    .filter(DM_TAG::equals)
                    .map(dmTag -> disambiguationMatchRules()
                            .orElseGet(rule::getMatch))
                    .orElseGet(rule::getMatch);
        }
        return playOffMatchRules(match.getMid());
    }

    public MatchRules playOffMatchRules() {
        return playOffMatchRules(null);
    }

    private MatchRules playOffMatchRules(Mid mid) {
        return rule.getPlayOff().map(po -> po.getMatch().orElse(rule.getMatch()))
                .orElseThrow(() -> internalError("match " + mid
                        + " without group in tid  " + tid));
    }

    public Stream<ParticipantMemState> findBidsByCategory(Cid cid) {
        return participants().filter(p -> p.getCid().equals(cid));
    }

    public Stream<GroupInfo> findGroupsByCategory(Cid cid) {
        return groups.values().stream().filter(gi -> gi.getCid().equals(cid));
    }

    public Collection<Bid> findBidsByUid(Uid uid) {
        return ofNullable(uidCid2Bid.get(uid))
                .map(Map::values)
                .orElseThrow(() -> notFound("User doesn't participant in the tournament"));
    }

    public Bid findBidByMidAndUid(MatchInfo m, Uid uid) {
        return ofNullable(uidCid2Bid.get(uid))
                .flatMap(map -> ofNullable(map.get(m.getCid())))
                .orElseThrow(() -> notFound("User doesn't participant"));
    }

    public Bid registerParticipant(ParticipantMemState participant) {
        participants.put(participant.getBid(), participant);
        uidCid2Bid.computeIfAbsent(
                participant.getUid(), k -> new HashMap<>())
                .put(participant.getCid(), participant.getBid());
        return participant.getBid();
    }

    public static final Set<BidState> ACTIVE_BID_STATES = ImmutableSet.of(
            Want, Paid, Here, Wait, Play, Expl, Lost, Win1, Win2, Win3);

    public long countActiveEnlistments(Uid uid) {
        return findBidsByUid(uid)
                .stream()
                .map(this::getParticipant)
                .map(ParticipantMemState::getBidState)
                .filter(ACTIVE_BID_STATES::contains)
                .count();
    }

    public Bid allocateBidFor(Cid cid, Uid uid) {
        return ofNullable(getUidCid2Bid().get(uid))
                .flatMap(m -> ofNullable(m.get(cid)))
                .orElseGet(() -> getNextBid().next());
    }

    public String toString() {
        return "trMemSt id=" + tid + ", st=" + state + ", hc=" + hashCode();
    }

    public Stream<ParticipantMemState> groupBids(Optional<Gid>  oGid) {
        return participants().filter(p -> p.getGid().equals(oGid));
    }

    public List<ParticipantMemState> findBidsByGroup(Gid gid) {
        return groupBids(of(gid)).collect(toList());
    }

    public CastingLotsRule casting() {
        return rule.getCasting();
    }
}
