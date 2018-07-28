package org.dan.ping.pong.app.tournament;

import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

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

    public Optional<Tid> parentTidO() {
        return parent.map(TidRelation::getTid);
    }

    public Tid parentTid() {
        return parentTidO().orElseThrow(
                () -> internalError("no parent tournament"));
    }

    public TournamentRelationType findRelTypeByTid(Tid tid) {
        return children.entrySet().stream()
                .filter(e -> e.getValue().equals(tid))
                .map(Map.Entry::getKey)
                .findAny()
                .orElseThrow(() -> internalError("tid " + tid + " is not child of"));
    }
}
