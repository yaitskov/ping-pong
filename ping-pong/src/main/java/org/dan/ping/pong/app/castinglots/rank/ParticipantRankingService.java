package org.dan.ping.pong.app.castinglots.rank;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import org.dan.ping.pong.app.castinglots.CastingLotsDao;
import org.dan.ping.pong.app.tournament.ParticipantMemState;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class ParticipantRankingService {
    public List<ParticipantMemState> sort(List<ParticipantMemState> bids,
            CastingLotsRule rule) {
        switch (rule.getPolicy()) {
            case SignUp:
                return sortBySignUp(bids, rule.getDirection());
            case ProvidedRating:
                return sortByProvidedRating(bids, rule.getDirection());
            default:
                throw internalError("Ranking policy " + rule.getPolicy() + " is not implemented");
        }
    }

    @Inject
    private CastingLotsDao castingLotsDao;

    private List<ParticipantMemState> sortByProvidedRating(List<ParticipantMemState> bids,
            OrderDirection direction) {
        Map<Integer, ParticipantMemState> participantIdx =
                bids.stream().collect(toMap(o -> o.getUid().getId(), o -> o));
        final List<ParticipantMemState> orderedBids = castingLotsDao.loadRanks(bids.get(0).getTid(),
                participantIdx.keySet(), direction)
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
                       comparingInt(bid -> bid.getUid().getId())))
                .collect(toList());
    }
}
