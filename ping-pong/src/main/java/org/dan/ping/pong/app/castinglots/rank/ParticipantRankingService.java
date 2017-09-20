package org.dan.ping.pong.app.castinglots.rank;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import org.dan.ping.pong.app.tournament.ParticipantMemState;

import java.util.List;

public class ParticipantRankingService {
    public List<ParticipantMemState> sort(List<ParticipantMemState> bids, CastingLotsRule rule) {
        switch (rule.getPolicy()) {
            case SignUp:
                return sortBySignUp(bids, rule.getDirection());
            default:
                throw internalError("Ranking policy " + rule.getPolicy() + " is not implemented");
        }
    }

    private List<ParticipantMemState> sortBySignUp(List<ParticipantMemState> bids, OrderDirection direction) {
        return bids.stream().sorted(
                direction.setupOrder(
                       comparingInt(bid -> bid.getUid().getId())))
                .collect(toList());
    }
}
