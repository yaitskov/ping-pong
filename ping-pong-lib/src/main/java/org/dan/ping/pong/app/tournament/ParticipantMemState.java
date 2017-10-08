package org.dan.ping.pong.app.tournament;

import static org.dan.ping.pong.app.bid.BidState.Lost;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.app.user.UserLink;

import java.util.Optional;

@Getter
@Setter
@Builder
public class ParticipantMemState {
    public static final int FILLER_LOSER_UID = 1;
    private final Tid tid;
    private final Uid uid;
    private BidState bidState;
    private Optional<Integer> gid = Optional.empty();
    private int cid;
    private String name;

    public String toString() {
        return "bid(" + uid + ", " + tid + ", " + cid + ")";
    }

    public BidState getState() {
        return bidState;
    }

    public UserLink toLink() {
        return UserLink.builder()
                .uid(uid.getId())
                .name(name)
                .build();
    }

    public static ParticipantMemState createLoserBid(Tid tid, int cid) {
        return ParticipantMemState.builder()
                .tid(tid)
                .cid(cid)
                .name(" - ")
                .bidState(Lost)
                .uid(new Uid(FILLER_LOSER_UID))
                .build();
    }
}
