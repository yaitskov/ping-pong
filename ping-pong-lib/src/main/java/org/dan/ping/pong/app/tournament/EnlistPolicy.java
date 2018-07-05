package org.dan.ping.pong.app.tournament;

import static java.util.Optional.ofNullable;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import org.dan.ping.pong.app.bid.Uid;

public enum EnlistPolicy {
    @JsonProperty("OT")
    ONCE_PER_TOURNAMENT {
        @Override
        public void validate(TournamentMemState tournament, int cid, Uid uid) {
            ofNullable(tournament.getUidCid2Bid().get(uid))
                    .ifPresent((m) -> {
                        throw badRequest(MULTIPLE_TOURNAMENT_ENLISTMENT, "uid", uid);
                    });
        }
    },
    @JsonProperty("OC")
    ONCE_PER_CATEGORY {
        @Override
        public void validate(TournamentMemState tournament, int cid, Uid uid) {
            ofNullable(tournament.getUidCid2Bid().get(uid))
                    .flatMap(m -> ofNullable(m.get(cid)))
                    .ifPresent((bid) -> {
                        throw badRequest(MULTIPLE_CATEGORY_ENLISTMENT,
                                ImmutableMap.of("bid", bid, "uid", uid));
                    });
        }
    };

    public static final String MULTIPLE_CATEGORY_ENLISTMENT = "Multiple category enlistment";
    public static final String MULTIPLE_TOURNAMENT_ENLISTMENT = "Multiple tournament enlistment";

    public abstract void validate(TournamentMemState tournamentMemState, int cid, Uid uid);
}
