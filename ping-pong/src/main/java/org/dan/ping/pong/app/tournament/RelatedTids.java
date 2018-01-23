package org.dan.ping.pong.app.tournament;

import lombok.Builder;
import lombok.Getter;

import java.util.Optional;

@Getter
@Builder
public class RelatedTids {
    private Optional<Tid> parent;
    private Optional<Tid> child;
}
