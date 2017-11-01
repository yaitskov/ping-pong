package org.dan.ping.pong.app.tournament;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.dan.ping.pong.app.bid.BidState;

import java.util.Optional;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EnlistOffline implements Enlist {
    private int cid;
    private Tid tid;
    private String name;
    private BidState bidState;
    private Optional<Integer> providedRank = Optional.empty();
    private Optional<Integer> groupId = Optional.empty();

    public static class EnlistOfflineBuilder {
        BidState bidState = BidState.Here;
        Optional<Integer> providedRank = Optional.empty();
        Optional<Integer> groupId = Optional.empty();
    }
}
