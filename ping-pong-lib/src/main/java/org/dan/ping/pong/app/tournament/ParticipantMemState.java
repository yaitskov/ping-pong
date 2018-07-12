package org.dan.ping.pong.app.tournament;

import static org.dan.ping.pong.app.bid.BidState.Lost;
import static org.dan.ping.pong.app.tournament.TournamentMemState.TID;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import com.google.common.collect.ImmutableMap;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.app.bid.ParticipantLink;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.category.Cid;
import org.dan.ping.pong.app.group.Gid;
import org.dan.ping.pong.app.user.UserLink;
import org.dan.ping.pong.app.user.UserLinkIf;

import java.time.Instant;
import java.util.Optional;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ParticipantMemState implements UserLinkIf {
    public static final Uid FILLER_LOSER_UID = new Uid(1);
    public static final Bid FILLER_LOSER_BID = new Bid(1);
    public static final String UID = "UID";
    public static final String BID = "bid";

    private Tid tid;
    private Uid uid;
    private Bid bid;
    private BidState bidState;
    private Optional<Gid> gid = Optional.empty();
    private Cid cid;
    private String name;
    private Instant enlistedAt;
    private Instant updatedAt;

    public static class ParticipantMemStateBuilder {
        Optional<Gid> gid = Optional.empty();
    }

    public Gid gid() {
        return gid.orElseThrow(() -> internalError(
                "Participant is not in a group",
                ImmutableMap.of(TID, tid, UID, uid)));
    }

    public String toString() {
        return "bid(" + uid + ", " + tid + ", " + cid + ")";
    }

    public BidState state() {
        return bidState;
    }

    public UserLink toLink() {
        return UserLink.builder()
                .uid(uid)
                .name(name)
                .build();
    }

    public ParticipantLink toBidLink() {
        return ParticipantLink.builder()
                .bid(bid)
                .name(name)
                .build();
    }

    public static ParticipantMemState createLoserBid(Tid tid, Cid cid) {
        return createLoserBid(tid, cid, Lost);
    }

    public static ParticipantMemState createLoserBid(Tid tid, Cid cid, BidState state) {
        return ParticipantMemState.builder()
                .tid(tid)
                .cid(cid)
                .name(" - ")
                .bidState(state)
                .uid(FILLER_LOSER_UID)
                .bid(FILLER_LOSER_BID)
                .build();
    }
}
