package org.dan.ping.pong.app.tournament.rel;

import static java.util.Optional.ofNullable;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import lombok.Builder;
import lombok.Getter;
import org.dan.ping.pong.app.tournament.RelatedTids;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.TournamentMemState;

import java.util.Map;

@Getter
@Builder
public class TournamentGroup {
    private final Tid masterTid;
    private final Map<Tid, TournamentMemState> tournamentMap;
    private final RelatedTids relatedTids;

    public TournamentMemState tour(Tid tid) {
        return ofNullable(tournamentMap.get(tid)).orElseThrow(
                () -> internalError("Tournament  " + tid
                        + " is not in the group of " + masterTid));
    }
}
