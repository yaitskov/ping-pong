package org.dan.ping.pong.app.tournament;

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
}
