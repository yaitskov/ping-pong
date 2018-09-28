package org.dan.ping.pong.app.match;

import static com.google.common.collect.Sets.difference;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;
import static org.dan.ping.pong.app.match.AffectedMatches.NO_AFFECTED_MATCHES;
import static org.dan.ping.pong.app.match.AffectedMatches.ofResets;
import static org.dan.ping.pong.app.match.MatchBid.matchBidOf;
import static org.dan.ping.pong.app.match.MatchInfo.MID;
import static org.dan.ping.pong.app.match.MatchState.Auto;
import static org.dan.ping.pong.app.match.rule.service.GroupRuleParams.ofParams;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;
import static org.dan.ping.pong.sys.hash.HashAggregator.createHashAggregator;

import com.google.common.collect.Sets.SetView;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.group.Gid;
import org.dan.ping.pong.app.group.GroupService;
import org.dan.ping.pong.app.match.dispute.MatchSets;
import org.dan.ping.pong.app.match.rule.GroupParticipantOrder;
import org.dan.ping.pong.app.match.rule.GroupParticipantOrderService;
import org.dan.ping.pong.app.match.rule.GroupPosition;
import org.dan.ping.pong.app.match.rule.GroupPositionIdx;
import org.dan.ping.pong.app.playoff.PlayOffService;
import org.dan.ping.pong.app.sport.Sports;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.TournamentMemState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.inject.Inject;

@Slf4j
public class AffectedMatchesService {
    public static final String DONT_CHECK_HASH = "dont-check-hash";
    public static final String GID = "gid";

    @Inject
    private GroupService groupService;

    @Inject
    private PlayOffService playOffService;

    @Inject
    private Sports sports;

    public void validateEffectHash(TournamentMemState tournament,
            RescoreMatch rescore, AffectedMatches effectedMatches) {
        final Optional<String> presentedHash = rescore.getEffectHash();
        if (DONT_CHECK_HASH.equals(presentedHash.orElse(""))) {
            log.info("Skip hash check {} on rescoring mid {}",
                    presentedHash, rescore.getMid());
            return;
        }
        final Optional<String> expectedEffectHash = createHashAggregator()
                .hash(effectedMatches).currentHash();
        if (expectedEffectHash.equals(presentedHash)) {
            log.info("Hash check passed for {} on rescoring mid {}",
                    presentedHash, rescore.getMid());
            return;
        }
        log.info("Hash mismatch expected [{}] but [{}]",
                expectedEffectHash.orElse(""), presentedHash.orElse(""));
        throw badRequest(effectedMatches.createError(tournament, expectedEffectHash));
    }

    @Inject
    private AffectedConsoleMatchesService affectedConMatches;

    public AffectedMatches findEffectedMatches(
            TournamentMemState tournament, MatchInfo rescoringMatch,
            MatchSets newSets) {
        if (rescoringMatch.inGroup()) {
            return findEffectMatchesByMatchInGroup(tournament, rescoringMatch, newSets);
        } else {
            if (rescoringMatch.continues()) {
                return NO_AFFECTED_MATCHES;
            }
            return findEffectMatchesByMatchInPlayOff(tournament, rescoringMatch, newSets);
        }
    }

    private void findPlayOffAffectedMatches(TournamentMemState tournament,
            Bid bid, List<MatchInfo> matches, List<MatchBid> result) {
        matches.stream()
                .filter(m -> m.getParticipantIdScore().containsKey(bid))
                .forEach(m -> {
                    result.add(matchBidOf(m.getMid(), bid));
                    m.getWinnerId().ifPresent(wUid -> {
                        if (wUid.equals(bid)) {
                            m.getWinnerMid().ifPresent(wMid ->
                                    findPlayOffAffectedMatches(tournament, bid,
                                            singletonList(tournament.getMatchById(wMid)), result));
                            m.getLoserMid().ifPresent(lMid ->
                                    findPlayOffAffectedMatches(tournament,
                                            m.getOpponentBid(bid)
                                                    .orElseThrow(() -> internalError("no loser")),
                                            singletonList(tournament.getMatchById(lMid)), result));
                        } else {
                            m.getWinnerMid().ifPresent(wMid ->
                                    findPlayOffAffectedMatches(tournament,
                                            m.getOpponentBid(bid)
                                                    .orElseThrow(() -> internalError("no winner")),
                                            singletonList(tournament.getMatchById(wMid)), result));
                            m.getLoserMid().ifPresent(lMid ->
                                    findPlayOffAffectedMatches(tournament,
                                            bid,
                                            singletonList(tournament.getMatchById(lMid)), result));

                        }
                    });
                    if (m.getState() == Auto) {
                        m.getLoserMid().ifPresent(lMid ->
                                findPlayOffAffectedMatches(tournament,
                                        bid,
                                        singletonList(tournament.getMatchById(lMid)), result));
                    }
                });
    }

    private static List<MatchInfo> replaceMatch(MatchInfo alternative, List<MatchInfo> matches) {
        final List<MatchInfo> result = concat(
                Stream.of(alternative),
                matches.stream()
                        .filter(mm -> !mm.getMid().equals(alternative.getMid())))
                .collect(toList());
        if (result.size() != matches.size()) {
            throw internalError("Match is not in the set", MID, alternative.getMid());
        }
        return result;
    }

    private AffectedMatches findAffectedByDisambiguationMatch(TournamentMemState tournament,
            MatchInfo newMatch, List<MatchInfo> allGroupMatches) {
        final List<MatchInfo> newAllGroupMatches = replaceMatch(newMatch, allGroupMatches);
        final ChildrenAffectedTournaments conAff = affectedConMatches.create();
        return AffectedMatches.builder()
                .consoleAffect(conAff.getAffects())
                .toBeReset(playOffMatchesAffectedByGroupWithPossibleDm(tournament,
                        allGroupMatches, newAllGroupMatches, conAff))
                .build();
    }

    private AffectedMatches findEffectMatchesByMatchInGroup(
            TournamentMemState tournament, MatchInfo oldMatch,
            MatchSets newSets) {
        final MatchInfo newMatch = sports.alternativeSets(tournament, oldMatch, newSets);
        final List<MatchInfo> allGroupMatches = groupService.findAllMatchesInGroup(
                tournament, oldMatch.groupId());
        if (oldMatch.disambiguationP() || tournament.disambiguationMatchNotPossible()) {
            return findAffectedByDisambiguationMatch(tournament, newMatch, allGroupMatches);
        } else {
            return findAffectedByOriginMatch(tournament, oldMatch, newMatch, allGroupMatches);
        }
    }

    private int countIncompleteMatches(List<MatchInfo> matches) {
        return (int) matches.stream().filter(MatchInfo::continues).count();
    }

    private List<Bid> bidsByGroup(TournamentMemState tournament, Optional<Gid> oGid) {
        return tournament.groupBids(oGid)
                .map(ParticipantMemState::getBid)
                .collect(toList());
    }

    private List<MatchBid> playOffMatchesAffectedByUids(
            TournamentMemState tournament, List<Bid> bids) {
        final List<MatchInfo> basePlayOffMatches = playOffService
                .findBaseMatches(tournament,
                        tournament.getBid(bids.get(0)).getCid(),
                        Optional.empty());
        final List<MatchBid> result = new ArrayList<>();
        bids.forEach(
                uid -> findPlayOffAffectedMatches(
                        tournament, uid, basePlayOffMatches, result));
        return result;
    }

    private Set<MatchParticipants> uidsOfMatches(List<MatchInfo> matches) {
        return matches.stream().map(MatchParticipants::create).collect(toSet());
    }

    private static List<MatchInfo> filterOrigin(List<MatchInfo> groupMatches) {
        return groupMatches.stream()
                .filter(m -> !m.getTag().isPresent())
                .collect(toList());
    }

    private AffectedMatches findAffectedByOriginMatch(TournamentMemState tournament,
            MatchInfo ogmi, MatchInfo gmiNewScore, List<MatchInfo> allGroupMatches) {
        final List<MatchInfo> originMatches = filterOrigin(allGroupMatches);
        final int incompleteOriginMatches = countIncompleteMatches(originMatches);
        final List<MatchInfo> newOriginMatches = replaceMatch(gmiNewScore, originMatches);
        final int newIncompleteOriginMatches = countIncompleteMatches(newOriginMatches);
        if (incompleteOriginMatches > 0) {
            if (newIncompleteOriginMatches > 0) {
                return NO_AFFECTED_MATCHES;
            } else {
                return findPossibleDisambiguationMatchesToGenerate(
                        tournament, newOriginMatches);
            }
        } else {
            final List<MatchInfo> presentDmMatches = findDisambiguationMatchSet(allGroupMatches);

            if (newIncompleteOriginMatches > 0) {
                return findAffectedMatchesIfAppearsIncompleteMatch(
                        tournament, ogmi, presentDmMatches);
            } else {
                return findAffectedMatchesIfOriginMatchesStayComplete(tournament,
                        allGroupMatches, newOriginMatches, presentDmMatches);
            }
        }
    }

    private AffectedMatches findAffectedMatchesIfAppearsIncompleteMatch(
            TournamentMemState tournament, MatchInfo ogmi,
            List<MatchInfo> presentDmMatches) {
        return AffectedMatches.builder()
                .toBeCreatedDm(emptySet())
                .toBeRemovedDm(presentDmMatches.stream()
                        .map(MatchInfo::getMid).collect(toSet()))
                .toBeReset(playOffMatchesAffectedByUids(tournament,
                        bidsByGroup(tournament, ogmi.getGid())))
                .consoleAffect(affectedConMatches.unlistEverybodyInCategory(
                        ogmi.getCid(), tournament))
                .build();
    }

    private AffectedMatches findAffectedMatchesIfOriginMatchesStayComplete(
            TournamentMemState tournament, List<MatchInfo> allGroupMatches,
            List<MatchInfo> newOriginMatches, List<MatchInfo> presentDmMatches) {
        final Set<MatchParticipants> newDmMatchesToGenerate
                = findPossibleDisambiguationMatchesToGenerate(tournament, newOriginMatches)
                .getToBeCreatedDm();
        final Set<MatchParticipants> uidsOfPresentDmMatches = uidsOfMatches(presentDmMatches);
        final Set<Mid> midsToBeRemoved = presentDmMatches.stream()
                .filter(m2 -> newDmMatchesToGenerate.stream()
                        .noneMatch(dm -> dm.hasAll(m2.bids())))
                .map(MatchInfo::getMid)
                .collect(toSet());
        final SetView<MatchParticipants> toBeCreated = difference(
                newDmMatchesToGenerate, uidsOfPresentDmMatches);
        final ChildrenAffectedTournaments conAff = affectedConMatches.create();
        return AffectedMatches.builder()
                .toBeCreatedDm(toBeCreated)
                .toBeRemovedDm(midsToBeRemoved)
                .consoleAffect(conAff.getAffects())
                .toBeReset(playOffMatchesAffectedByGroupWithPossibleDm(tournament,
                        allGroupMatches,
                        concat(
                                concat(newOriginMatches.stream(),
                                        presentDmMatches.stream()
                                        .filter(m2 -> newDmMatchesToGenerate
                                                .containsAll(m2.bids()))),
                                newDmMatchesToGenerate.stream()
                                        .filter(m2 -> !presentDmMatches.contains(m2))
                                        .map(MatchParticipants::toFakeMatch))
                                .collect(toList()), conAff))
                .build();
    }

    private List<MatchBid> playOffMatchesAffectedByGroupWithPossibleDm(
            TournamentMemState tournament,
            List<MatchInfo> oldGroupMatches,
            List<MatchInfo> newGroupMatches, ChildrenAffectedTournaments conAff) {
        final Gid gid = oldGroupMatches.get(0).groupId();
        final Set<Bid> groupBids = tournament.bidsInGroup(gid);
        final GroupParticipantOrder oldOrder = groupParticipantOrderService
                .findOrder(ofParams(Optional.of(gid), tournament, oldGroupMatches,
                        tournament.orderRules(),
                        new HashSet<>(groupBids)));
        final GroupParticipantOrder newOrder = groupParticipantOrderService
                .findOrder(ofParams(Optional.of(gid), tournament, newGroupMatches,
                        tournament.orderRules(),
                        groupBids));

        final List<Bid> wasDeterminedBids = oldOrder.determinedBids();
        final List<Bid> newDeterminedBids = newOrder.determinedBids();
        final List<Bid> affectedBids = new ArrayList<>();
        for (int i = 0; i < wasDeterminedBids.size(); ++i) {
            Bid u = wasDeterminedBids.get(i);
            if (u == null /*was not determined*/) {
                continue;
            }
            if (i >= newDeterminedBids.size() || !u.equals(newDeterminedBids.get(i))) {
                affectedBids.add(u);
            }
        }
        if (affectedBids.isEmpty()) {
            return emptyList();
        }
        affectedConMatches.affectedByGroup(
                tournament, conAff, wasDeterminedBids, newDeterminedBids);
        return playOffMatchesAffectedByUids(tournament, affectedBids);
    }

    @Inject
    private GroupParticipantOrderService groupParticipantOrderService;

    private AffectedMatches findPossibleDisambiguationMatchesToGenerate(
            TournamentMemState tournament, List<MatchInfo> originMatches) {
        final Gid gid = originMatches.get(0).groupId();
        final Optional<Gid> oGid = Optional.of(gid);
        final GroupParticipantOrder order = groupParticipantOrderService
                .findOrder(ofParams(oGid,
                        tournament, originMatches, tournament.orderRules(),
                        tournament.bidsInGroup(gid)));

        if (order.unambiguous()) {
            return NO_AFFECTED_MATCHES;
        }

        final Set<MatchParticipants> disambiguationMatches = new HashSet<>();
        for (GroupPositionIdx ambiPos : order.getAmbiguousPositions()) {
             final GroupPosition gPosition = order.getPositions().get(ambiPos);
             final List<Bid> competingBids = new ArrayList<>(
                     gPosition.getCompetingBids());
             for (int i = 0; i < competingBids.size(); ++i) {
                 final Bid bid1 = competingBids.get(i);
                 for (int j = i + 1; j < competingBids.size(); ++j) {
                     disambiguationMatches.add(
                             new MatchParticipants(bid1, competingBids.get(j)));
                 }
             }
        }

        return AffectedMatches.builder()
                .toBeCreatedDm(disambiguationMatches)
                .toBeReset(playOffMatchesAffectedByUids(tournament,
                        bidsByGroup(tournament, oGid)))
                .consoleAffect(affectedConMatches.unlistEverybodyInCategory(
                        originMatches.get(0).getCid(), tournament))
                .build();
    }

    private List<MatchInfo> findDisambiguationMatchSet(List<MatchInfo> matches) {
        return matches.stream().filter(MatchInfo::disambiguationP).collect(toList());
    }

    private AffectedMatches findEffectMatchesByMatchInPlayOff(
            TournamentMemState tournament, MatchInfo mInfo,
            MatchSets newSets) {
        final Optional<Bid> newWinner = sports.findNewWinnerBid(tournament, newSets, mInfo);
        if (newWinner.equals(mInfo.getWinnerId())) {
            return NO_AFFECTED_MATCHES;
        }
        final List<MatchBid> result = new ArrayList<>();
        mInfo.getParticipantIdScore().keySet()
                .forEach(uid -> findPlayOffAffectedMatches(
                        tournament, uid, singletonList(mInfo), result));
        return ofResets(result.stream()
                .filter(m -> !m.getMid().equals(mInfo.getMid()))
                .mapToInt()
                .collect(toList())).deduplicate();
    }
}
