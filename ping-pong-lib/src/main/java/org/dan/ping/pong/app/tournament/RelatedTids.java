package org.dan.ping.pong.app.tournament;

import lombok.Builder;
import lombok.Getter;
import org.dan.ping.pong.app.tournament.console.TournamentRelationType;

import java.util.Map;
import java.util.Optional;

@Getter
@Builder
public class RelatedTids {
    private final Optional<TidRelation> parent;
    private final Map<TournamentRelationType, Tid> children;

    public Optional<Tid> parentTid() {
        return parent.map(TidRelation::getTid);
    }
}
