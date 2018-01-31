package org.dan.ping.pong.app.tournament;

import static org.dan.ping.pong.app.bid.BidState.Expl;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.sys.validation.TidBodyRequired;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ExpelParticipant {
    @Valid
    @TidBodyRequired
    private Tid tid;

    @Valid
    @NotNull
    private Uid uid;

    @NotNull
    private BidState targetBidState = Expl;

    public static class ExpelParticipantBuilder {
        BidState targetBidState = Expl;
    }
}
