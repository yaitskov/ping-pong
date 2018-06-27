package org.dan.ping.pong.app.tournament;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.app.bid.Uid;
import org.jooq.types.UByte;

import java.util.Optional;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EnlistOffline implements Enlist {
    @Min(value = 1, message = "Category id is missing")
    @Max(value = UByte.MAX_VALUE, message = "Category id overflow")
    private int cid;
    @Valid
    private Tid tid;
    @Valid
    private Uid uid;
    private BidState bidState;
    private Optional<Integer> providedRank = Optional.empty();
    private Optional<Integer> groupId = Optional.empty();

    public static class EnlistOfflineBuilder {
        BidState bidState = BidState.Here;
        Optional<Integer> providedRank = Optional.empty();
        Optional<Integer> groupId = Optional.empty();
    }
}
