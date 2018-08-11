package org.dan.ping.pong.app.match;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.dan.ping.pong.app.group.ConsoleTournament.NO;
import static org.dan.ping.pong.app.match.MatchBid.matchBidOf;
import static org.dan.ping.pong.app.match.MatchTag.DISAMBIGUATION;
import static org.dan.ping.pong.app.tournament.console.TournamentRelationType.ConGru;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import com.google.common.collect.ImmutableMap;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.category.CategoryMemState;
import org.dan.ping.pong.app.category.Cid;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.app.tournament.rel.RelatedTournamentsService;
import org.dan.ping.pong.app.tournament.rel.TournamentGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

public class AffectedConsoleMatchesService {
    @Inject
    private RelatedTournamentsService relatedTournaments;

    public AffectedMatches findAffectedMatches(
            TournamentMemState conTour, Cid cid, List<Bid> orderedConBids) {
        return null;
    }

    public ChildrenAffectedTournaments create() {
        return new ChildrenAffectedTournaments(new HashMap<>());
    }

    public void affectedByGroup(TournamentMemState mTour,
            ChildrenAffectedTournaments conAff,
            List<Bid> wasDeterminedBids, List<Bid> nowDeterminedBids) {
        if (mTour.groupRules().getConsole() == NO) {
            return;
        }
        final TournamentGroup tourGroup = relatedTournaments.groupOfConTours(mTour);
        final TournamentMemState conTour = tourGroup.tourByType(ConGru);
        final Cid mCid = mTour.getBidOrEx(wasDeterminedBids.get(0)).getCid();
        final Cid conCid = conTour.findCidByName(mTour.getCategory(mCid));
        final CategoryMemState conCat = conTour.getCategory(conCid);
        final LineUpDiff lineUpDiff = buildLineUpDiff(wasDeterminedBids, nowDeterminedBids,
                mTour.groupRules().getQuits());

        conAff.update(conTour.getTid(), (affect) -> {
            switch (conCat.getState()) {
                case End:
                case Ply:
                    affect.setToBeReset(findMatchesForReset(conCat.getCid(), conTour, lineUpDiff));
                    affect.setToBeRemovedDm(findDmMatchesForRemove(conCat.getCid(), conTour));
                    // go
                case Drt:
                    affect.setLineUpDiff(Optional.of(lineUpDiff));
                    break;
                default:
                    throw internalError("behavior for state "
                            + conCat.getState() + " not implemented");
            }
        });
    }

    private List<MatchBid> findMatchesForReset(
            Cid conCid, TournamentMemState conTour, LineUpDiff lineUpDiff) {
        final Set<Bid> movedBids = lineUpDiff.toBeMoved();
        final List<MatchBid> toBeReset = new ArrayList<>();
        conTour.findMatchesByCid(conCid)
                .filter(m -> m.getTag().map(tag -> !tag.getPrefix().equals(DISAMBIGUATION))
                        .orElse(true))
                .forEach(m -> m.bids()
                        .forEach(bid -> {
                            if (movedBids.contains(bid)) {
                                toBeReset.add(matchBidOf(m.getMid(), bid));
                            }
                        }));
        return toBeReset;
    }

    private Set<Mid> findDmMatchesForRemove(Cid conCid, TournamentMemState conTour) {
        return conTour.findMatchesByCid(conCid)
                .filter(m -> m.getTag()
                        .map(tag -> tag.getPrefix().equals(DISAMBIGUATION))
                        .orElse(false))
                .map(MatchInfo::getMid)
                .collect(toSet());
    }

    private LineUpDiff buildLineUpDiff(
            List<Bid> wasDeterminedBids, List<Bid> nowDeterminedBids, int quits) {
        final Set<Bid> toBeEnlisted = new HashSet<>();
        final List<Bid> toBeUnlisted = new ArrayList<>();
        // hard code of master outcome order
        for (int i = quits; i < wasDeterminedBids.size(); ++i) {
            final Bid was = wasDeterminedBids.get(i);
            final Bid now = nowDeterminedBids.get(i);

            if (!was.equals(now)) {
                toBeEnlisted.add(now);
                toBeUnlisted.add(was);
            }
        }

        return LineUpDiff.builder()
                .toBeEnlisted(toBeEnlisted)
                .toBeUnlistedOrMoved(toBeUnlisted)
                .build();
    }

    public Map<Tid, AffectedMatches> unlistEverybodyInCategory(
            Cid mCid, TournamentMemState mTour) {
        if (mTour.groupRules().getConsole() == NO) {
            return emptyMap();
        }
        final TournamentGroup tourGroup = relatedTournaments.groupOfConTours(mTour);
        final TournamentMemState conTour = tourGroup.tourByType(ConGru);
        final Cid conCid = conTour.findCidByName(mTour.getCategory(mCid));

        return ImmutableMap.of(conTour.getTid(), AffectedMatches
                .builder()
                .toBeRemovedDm(conTour.findMatchesByCid(conCid)
                        .map(MatchInfo::getMid)
                        .collect(toSet()))
                .lineUpDiff(Optional.of(LineUpDiff
                        .builder()
                        .toBeEnlisted(emptySet())
                        .toBeUnlistedOrMoved(conTour
                                .findBidsByCategory(conCid)
                                .map(ParticipantMemState::getBid)
                                .collect(toList()))
                        .build()))
                .build());
    }
}
