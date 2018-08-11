package org.dan.ping.pong.app.match;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dan.ping.pong.app.tournament.Tid;

import java.util.Map;
import java.util.function.Consumer;


@Getter
@RequiredArgsConstructor
public class ChildrenAffectedTournaments {
    private final Map<Tid, AffectedMatches> affects;

    public void update(Tid tid, Consumer<AffectedMatches> affectCb) {
        affects.compute(tid, (k, affect) -> {
           if (affect == null) {
               affect = AffectedMatches.builder().build();
           }
           affectCb.accept(affect);
           return affect;
        });
    }
}
