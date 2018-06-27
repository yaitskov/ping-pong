package org.dan.ping.pong.app.group;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.match.rule.reason.Reason;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class GroupParticipantResult {
    private Uid uid;
    private Bid bid;
    private String name;
    private Map<Bid, GroupMatchResult> originMatches;
    private Map<Bid, GroupMatchResult> dmMatches;
    private int seedPosition;
    private int finishPosition;
    private List<Optional<Reason>> reasonChain;
    private BidState state;
}
