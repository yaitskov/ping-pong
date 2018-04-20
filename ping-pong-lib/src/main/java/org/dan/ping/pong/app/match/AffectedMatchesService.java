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
import static org.dan.ping.pong.app.match.MatchInfo.MID;
import static org.dan.ping.pong.app.match.MatchState.Auto;
import static org.dan.ping.pong.app.match.rule.service.GroupRuleParams.ofParams;
import static org.dan.ping.pong.app.tournament.TournamentMemState.TID;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;
import static org.dan.ping.pong.sys.hash.HashAggregator.createHashAggregator;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets.SetView;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.group.GroupService;
import org.dan.ping.pong.app.match.dispute.MatchSets;
import org.dan.ping.pong.app.match.rule.GroupParticipantOrder;
import org.dan.ping.pong.app.match.rule.GroupParticipantOrderService;
import org.dan.ping.pong.app.match.rule.GroupPosition;
import org.dan.ping.pong.app.match.rule.GroupPositionIdx;
import org.dan.ping.pong.app.playoff.PlayOffService;
import org.dan.ping.pong.app.sport.Sports;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.TournamentMemState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
            Uid uid, List<MatchInfo> matches, List<MatchUid> result) {
        matches.stream()
                .filter(m -> m.getParticipantIdScore().containsKey(uid))
                .forEach(m -> {
                    result.add(MatchUid.builder().uid(uid).mid(m.getMid()).build());
                    m.getWinnerId().ifPresent(wUid -> {
                        if (wUid.equals(uid)) {
                            m.getWinnerMid().ifPresent(wMid ->
                                    findPlayOffAffectedMatches(tournament, uid,
                                            singletonList(tournament.getMatchById(wMid)), result));
                            m.getLoserMid().ifPresent(lMid ->
                                    findPlayOffAffectedMatches(tournament,
                                            m.getOpponentUid(uid)
                                                    .orElseThrow(() -> internalError("no loser")),
                                            singletonList(tournament.getMatchById(lMid)), result));
                        } else {
                            m.getWinnerMid().ifPresent(wMid ->
                                    findPlayOffAffectedMatches(tournament,
                                            m.getOpponentUid(uid)
                                                    .orElseThrow(() -> internalError("no winner")),
                                            singletonList(tournament.getMatchById(wMid)), result));
                            m.getLoserMid().ifPresent(lMid ->
                                    findPlayOffAffectedMatches(tournament,
                                            uid,
                                            singletonList(tournament.getMatchById(lMid)), result));

                        }
                    });
                    if (m.getState() == Auto) {
                        m.getLoserMid().ifPresent(lMid ->
                                findPlayOffAffectedMatches(tournament,
                                        uid,
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
            MatchInfo gmiNewScore, List<MatchInfo> allGroupMatches) {
        final List<MatchInfo> newAllGroupMatches = replaceMatch(gmiNewScore, allGroupMatches);
        return AffectedMatches.builder()
                .toBeCreated(emptySet())
                .toBeRemoved(emptySet())
                .toBeReset(playOffMatchesAffectedByGroupWithPossibleDm(tournament,
                        allGroupMatches,
                        newAllGroupMatches, true))
                .build();
    }

    private AffectedMatches findEffectMatchesByMatchInGroup(
            TournamentMemState tournament, MatchInfo gmi,
            MatchSets newSets) {
        final MatchInfo gmiNewScore = sports.alternativeSets(tournament, gmi, newSets);
        final List<MatchInfo> allGroupMatches = groupService.findAllMatchesInGroup(
                tournament, gmi.groupId());
        if (gmi.disambiguationP() || tournament.disambiguationMatchNotPossible()) {
            return findAffectedByDisambiguationMatch(tournament, gmiNewScore, allGroupMatches);
        } else {
            return findAffectedByOriginMatch(tournament, gmi, gmiNewScore, allGroupMatches);
        }
    }

    private int countIncompleteMatches(List<MatchInfo> matches) {
        return (int) matches.stream().filter(MatchInfo::continues).count();
    }

    private List<Uid> uidsByGroup(TournamentMemState tournament, Optional<Integer> ogid) {
        return tournament.getParticipants().values().stream()
                .filter(p -> p.getGid().equals(ogid))
                .map(ParticipantMemState::getUid)
                .collect(toList());
    }

    private List<MatchUid> playOffMatchesAffectedByUids(
            TournamentMemState tournament, List<Uid> uids) {
        final List<MatchInfo> basePlayOffMatches = playOffService
                .findBaseMatches(tournament,
                        tournament.getBid(uids.get(0)).getCid(),
                        Optional.empty());
        final List<MatchUid> result = new ArrayList<>();
        uids.forEach(
                uid -> findPlayOffAffectedMatches(
                        tournament, uid, basePlayOffMatches, result));
        return result;
    }

    private Set<MatchParticipants> uidsOfMatches(List<MatchInfo> matches) {
        return matches.stream().map(MatchParticipants::create).collect(toSet());
    }

    private AffectedMatches findAffectedByOriginMatch(TournamentMemState tournament,
            MatchInfo ogmi, MatchInfo gmiNewScore, List<MatchInfo> allGroupMatches) {
        final List<MatchInfo> originMatches = allGroupMatches.stream()
                .filter(m -> !m.getTag().isPresent()).collect(toList());
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
                return AffectedMatches.builder()
                        .toBeCreated(emptySet())
                        .toBeRemoved(presentDmMatches.stream()
                                .map(MatchInfo::getMid).collect(toSet()))
                        .toBeReset(playOffMatchesAffectedByUids(tournament,
                                uidsByGroup(tournament, ogmi.getGid())))
                        .build();
            } else {
                return findAffectedMatchesIfOriginMatchesStayComplete(tournament,
                        allGroupMatches, newOriginMatches, presentDmMatches);
            }
        }
    }

    private AffectedMatches findAffectedMatchesIfOriginMatchesStayComplete(
            TournamentMemState tournament, List<MatchInfo> allGroupMatches,
            List<MatchInfo> newOriginMatches, List<MatchInfo> presentDmMatches) {
        final Set<MatchParticipants> newDmMatchesToGenerate
                = findPossibleDisambiguationMatchesToGenerate(tournament, newOriginMatches)
                .getToBeCreated();
        final Set<MatchParticipants> uidsOfPresentDmMatches = uidsOfMatches(presentDmMatches);
        final Set<Mid> midsToBeRemoved = presentDmMatches.stream()
                .filter(m2 -> !newDmMatchesToGenerate.containsAll(m2.getUids()))
                .map(MatchInfo::getMid)
                .collect(toSet());
        final SetView<MatchParticipants> toBeCreated = difference(
                newDmMatchesToGenerate, uidsOfPresentDmMatches);
        return AffectedMatches.builder()
                .toBeCreated(toBeCreated)
                .toBeRemoved(midsToBeRemoved)
                .toBeReset(playOffMatchesAffectedByGroupWithPossibleDm(tournament,
                        allGroupMatches,
                        concat(
                                concat(newOriginMatches.stream(),
                                        presentDmMatches.stream()
                                        .filter(m2 -> newDmMatchesToGenerate
                                                .containsAll(m2.getUids()))),
                                newDmMatchesToGenerate.stream()
                                        .filter(m2 -> !presentDmMatches.contains(m2))
                                        .map(MatchParticipants::toFakeMatch))
                                .collect(toList()),
                        toBeCreated.isEmpty()))
                .build();
    }

    private List<MatchUid> playOffMatchesAffectedByGroupWithPossibleDm(
            TournamentMemState tournament,
            List<MatchInfo> allGroupMatches,
            List<MatchInfo> allNewGroupMatches,
            boolean noNewIncompleteMatches) {
        final int gid = allGroupMatches.get(0).groupId();
        final Set<Uid> groupUids = tournament.uidsInGroup(gid);
        final GroupParticipantOrder order = groupParticipantOrderService
                .findOrder(ofParams(gid, tournament, allGroupMatches,
                        tournament.orderRules(),
                        new HashSet<>(groupUids)));
        final GroupParticipantOrder newOrder = groupParticipantOrderService
                .findOrder(ofParams(gid, tournament, allNewGroupMatches,
                        tournament.orderRules(),
                        groupUids));

        validate(tournament, allNewGroupMatches,
                noNewIncompleteMatches, newOrder);

        final List<Uid> wasDeterminedUids = order.determinedUids();
        final List<Uid> newDeterminedUids = newOrder.determinedUids();
        final List<Uid> affectedUids = new ArrayList<>();
        for (int i = 0; i < wasDeterminedUids.size(); ++i) {
            Uid u = wasDeterminedUids.get(i);
            if (u == null /*was not determined*/) {
                continue;
            }
            if (i >= newDeterminedUids.size() || !u.equals(newDeterminedUids.get(i))) {
                affectedUids.add(u);
            }
        }
        if (affectedUids.isEmpty()) {
            return emptyList();
        }
        return playOffMatchesAffectedByUids(tournament, affectedUids);
    }

    private void validate(TournamentMemState tournament,
            List<MatchInfo> allNewCompleteGroupMatches, boolean noNewIncompleteMatches,
            GroupParticipantOrder newOrder) {
        tournament.getRule().getGroup().ifPresent(groupRules -> {
            if (!groupRules.getDisambiguationMatch().isPresent()) {
                 if (noNewIncompleteMatches && allNewCompleteGroupMatches
                         .stream().allMatch(m -> m.getWinnerMid().isPresent())) {
                     if (!newOrder.unambiguous()) {
                         throw internalError(
                                 "Tournament has a complete group with ambiguous score",
                                 ImmutableMap.of(TID, tournament.getTid(),
                                         GID, allNewCompleteGroupMatches.get(0).getGid()));
                     }
                 }
            }
        });
    }

    @Inject
    private GroupParticipantOrderService groupParticipantOrderService;

    private AffectedMatches findPossibleDisambiguationMatchesToGenerate(
            TournamentMemState tournament, List<MatchInfo> originMatches) {
        final int gid = originMatches.get(0).groupId();
        final GroupParticipantOrder order = groupParticipantOrderService
                .findOrder(ofParams(gid,
                        tournament, originMatches, tournament.orderRules(),
                        tournament.uidsInGroup(gid)));

        if (order.unambiguous()) {
            return NO_AFFECTED_MATCHES;
        }

        final Set<MatchParticipants> disambiguationMatches = new HashSet<>();
        for (GroupPositionIdx ambiPos : order.getAmbiguousPositions()) {
             final GroupPosition gPosition = order.getPositions().get(ambiPos);
             final List<Uid> competingUids = new ArrayList<>(
                     gPosition.getCompetingUids());
             for (int i = 0; i < competingUids.size(); ++i) {
                 final Uid uid1 = competingUids.get(i);
                 for (int j = i + 1; j < competingUids.size(); ++j) {
                     disambiguationMatches.add(
                             new MatchParticipants(uid1, competingUids.get(j)));
                 }
             }
        }

        return AffectedMatches.builder()
                .toBeReset(emptyList())
                .toBeRemoved(emptySet())
                .toBeCreated(disambiguationMatches)
                .build();
    }

    private List<MatchInfo> findDisambiguationMatchSet(List<MatchInfo> matches) {
        return matches.stream().filter(MatchInfo::disambiguationP).collect(toList());
    }

    private AffectedMatches findEffectMatchesByMatchInPlayOff(
            TournamentMemState tournament, MatchInfo mInfo,
            MatchSets newSets) {
        final Optional<Uid> newWinner = sports.findNewWinnerUid(tournament, newSets, mInfo);
        if (newWinner.equals(mInfo.getWinnerId())) {
            return NO_AFFECTED_MATCHES;
        }
        final List<MatchUid> result = new ArrayList<>();
        mInfo.getParticipantIdScore().keySet()
                .forEach(uid -> findPlayOffAffectedMatches(
                        tournament, uid, singletonList(mInfo), result));
        return ofResets(result.stream()
                .filter(m -> !m.getMid().equals(mInfo.getMid()))
                .collect(toList())).deduplicate();
    }
}
