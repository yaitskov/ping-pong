package org.dan.ping.pong.app.tournament;

import static java.util.Optional.ofNullable;
import static org.dan.ping.pong.app.tournament.TournamentMemState.ACTIVE_BID_STATES;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.category.Cid;

public enum EnlistPolicy {
    @JsonProperty("OT")
    ONCE_PER_TOURNAMENT {
        @Override
        public void validate(TournamentMemState tournament, Cid cid, Uid uid) {
            ofNullable(tournament.getUidCid2Bid().get(uid))
                    .ifPresent((m) -> {
                        final long activeEnlistments = tournament.countActiveEnlistments(uid);
                        if (activeEnlistments == 0
                                || (activeEnlistments == 1
                                && ofNullable(m.get(cid))
                                .map(tournament::getParticipant)
                                .map(ParticipantMemState::getBidState)
                                .map(ACTIVE_BID_STATES::contains)
                                .orElse(false))) {
                                return;
                        }
                        throw badRequest(MULTIPLE_TOURNAMENT_ENLISTMENT, "uid", uid);
                    });
        }
    },
    @JsonProperty("OC")
    ONCE_PER_CATEGORY {
        @Override
        public void validate(TournamentMemState tournament, Cid cid, Uid uid) {
            ofNullable(tournament.getUidCid2Bid().get(uid))
                    .flatMap(m -> ofNullable(m.get(cid)))
                    .map(tournament::getParticipant)
                    .ifPresent((par) -> {
                        if (ACTIVE_BID_STATES.contains(par.getBidState())) {
                            throw badRequest(MULTIPLE_CATEGORY_ENLISTMENT,
                                    ImmutableMap.of("bid", par.getBid(), "uid", uid));
                        }
                    });
        }
    };

    public static final String MULTIPLE_CATEGORY_ENLISTMENT = "Multiple category enlistment";
    public static final String MULTIPLE_TOURNAMENT_ENLISTMENT = "Multiple tournament enlistment";

    public abstract void validate(TournamentMemState tournamentMemState, Cid cid, Uid uid);
}
