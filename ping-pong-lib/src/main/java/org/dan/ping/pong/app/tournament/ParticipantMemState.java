package org.dan.ping.pong.app.tournament;

import static org.dan.ping.pong.app.bid.BidState.Lost;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.user.UserLink;

import java.time.Instant;
import java.util.Optional;

@Getter
@Setter
@Builder
public class ParticipantMemState {
    public static final Uid FILLER_LOSER_UID = new Uid(1);

    private final Tid tid;
    private final Uid uid;
    private BidState bidState;
    private Optional<Integer> gid = Optional.empty();
    private int cid;
    private String name;
    private Instant enlistedAt;
    private Instant updatedAt;

    public static class ParticipantMemStateBuilder {
        Optional<Integer> gid = Optional.empty();
    }

    public String toString() {
        return "bid(" + uid + ", " + tid + ", " + cid + ")";
    }

    public BidState getState() {
        return bidState;
    }

    public UserLink toLink() {
        return UserLink.builder()
                .uid(uid)
                .name(name)
                .build();
    }

    public static ParticipantMemState createLoserBid(Tid tid, int cid) {
        return createLoserBid(tid, cid, Lost);
    }

    public static ParticipantMemState createLoserBid(Tid tid, int cid, BidState state) {
        return ParticipantMemState.builder()
                .tid(tid)
                .cid(cid)
                .name(" - ")
                .bidState(state)
                .uid(FILLER_LOSER_UID)
                .build();
    }
}
