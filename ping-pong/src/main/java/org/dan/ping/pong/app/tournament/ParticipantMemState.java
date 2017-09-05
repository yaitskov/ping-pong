package org.dan.ping.pong.app.tournament;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.dan.ping.pong.app.bid.BidState;

@Getter
@Setter
@Builder
public class ParticipantMemState {
    private final Tid tid;
    private final Uid uid;
    private BidState bidState;
}
