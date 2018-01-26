package org.dan.ping.pong.app.tournament;

import lombok.Builder;
import lombok.Getter;

import java.util.Optional;

@Getter
@Builder
public class RelatedTids {
    private final Optional<Tid> parent;
    private final Optional<Tid> child;
}
