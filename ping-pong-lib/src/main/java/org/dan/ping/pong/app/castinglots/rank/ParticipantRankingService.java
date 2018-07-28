package org.dan.ping.pong.app.castinglots.rank;

import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.dan.ping.pong.app.castinglots.rank.ParticipantRankingPolicy.MasterOutcome;
import static org.dan.ping.pong.app.tournament.TournamentCache.TOURNAMENT_CACHE;
import static org.dan.ping.pong.app.tournament.TournamentCache.TOURNAMENT_RELATION_CACHE;
import static org.dan.ping.pong.app.tournament.TournamentType.Console;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import com.google.common.cache.LoadingCache;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.castinglots.CastingLotsDaoIf;
import org.dan.ping.pong.app.category.Cid;
import org.dan.ping.pong.app.group.GroupService;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.RelatedTids;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.app.tournament.TournamentResultEntry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

@Slf4j
public class ParticipantRankingService {
    public List<ParticipantMemState> sort(List<ParticipantMemState> bids,
            CastingLotsRule rule, TournamentMemState tournament) {
        switch (rule.getPolicy()) {
            case SignUp:
                return sortBySignUp(bids, rule.getDirection());
            case ProvidedRating:
                return sortByProvidedRating(bids, rule.getDirection());
            case Manual:
                return sortedManually(bids);
            case MasterOutcome:
                return sortByOutcomeInMasterTournament(bids, tournament);
            default:
                throw internalError("Ranking policy " + rule.getPolicy() + " is not implemented");
        }
    }

    @Inject
    @Named(TOURNAMENT_CACHE)
    private LoadingCache<Tid, TournamentMemState> tournamentCache;

    @Inject
    @Named(TOURNAMENT_RELATION_CACHE)
    private LoadingCache<Tid, RelatedTids> tournamentRelationCache;

    @Inject
    private GroupService groupService;

    @SneakyThrows
    private List<ParticipantMemState> sortByOutcomeInMasterTournament(
            List<ParticipantMemState> bids, TournamentMemState tournament) {
        if (tournament.getType() != Console) {
            throw internalError(MasterOutcome + " rank policy is only available in "
                    + Console + " tournament but tid " + tournament.getTid()
                    + " is " + tournament.getType());
        }
        final RelatedTids relatedTids = tournamentRelationCache.get(tournament.getTid());
        final Tid masterTid = relatedTids.parentTidO()
                .orElseThrow(() -> internalError("tid "
                        + tournament.getTid() + " has no master tournament"));

        final TournamentMemState masterTournament = tournamentCache.get(masterTid);
        final Map<Cid, Map<Bid, Integer>> cid2Bid2Position = new HashMap<>();

        return bids.stream().map(bid -> {
            final Cid masterCid = masterTournament.getParticipant(bids.get(0).getBid()).getCid();
            Map<Bid, Integer> uid2Position = cid2Bid2Position.get(masterCid);
            if (uid2Position == null) {
                log.info("Find order in cid {} of tid {}",
                        masterCid, masterTournament.getTid());
                final List<TournamentResultEntry> groupOrdered = groupService
                        .resultOfAllGroupsInCategory(masterTournament, masterCid);
                cid2Bid2Position.put(masterCid, uid2Position = new HashMap<>());
                for (int i = 0; i < groupOrdered.size(); ++i) {
                    uid2Position.put(groupOrdered.get(i).getUser().getBid(), i);
                }
            }
            return BidIdx.builder()
                    .index(ofNullable(uid2Position.get(bid.getBid()))
                            .orElseGet(() -> {
                                log.warn("no bid {} in cid {}", bid.getBid(), masterCid);
                                return 0;
                            }))
                    .bid(bid.getBid())
                    .build();
        }).collect(Collectors.toCollection(() -> new TreeSet<>(BidIdx.uidIdxComparator)))
                .stream()
                .map(uidIdx -> tournament.getParticipant(uidIdx.getBid()))
                .collect(toList());
    }

    @Inject
    private CastingLotsDaoIf castingLotsDao;

    private List<ParticipantMemState> sortByProvidedRating(List<ParticipantMemState> bids,
            OrderDirection direction) {
        Map<Uid, ParticipantMemState> participantIdx =
                bids.stream().collect(toMap(ParticipantMemState::getUid, o -> o));
        final List<ParticipantMemState> orderedBids = castingLotsDao.loadRanks(bids.get(0).getTid(),
                participantIdx.keySet(), direction)
                .stream()
                .map(participantIdx::remove)
                .collect(toList());
        orderedBids.addAll(participantIdx.values());
        return orderedBids;
    }

    private List<ParticipantMemState> sortedManually(List<ParticipantMemState> bids) {
        Map<Uid, ParticipantMemState> participantIdx =
                bids.stream().collect(toMap(ParticipantMemState::getUid, o -> o));
        final List<ParticipantMemState> orderedBids = castingLotsDao
                .loadSeed(bids.get(0).getTid(), participantIdx.keySet())
                .stream()
                .map(participantIdx::remove)
                .collect(toList());
        orderedBids.addAll(participantIdx.values());
        return orderedBids;

    }

    private List<ParticipantMemState> sortBySignUp(List<ParticipantMemState> bids,
            OrderDirection direction) {
        return bids.stream().sorted(
                direction.setupOrder(
                       comparing(ParticipantMemState::getBid)))
                .collect(toList());
    }
}
