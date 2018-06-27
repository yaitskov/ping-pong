package org.dan.ping.pong.app.match.dispute;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.match.Mid;
import org.dan.ping.pong.app.tournament.Tid;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DisputeClaimRequest {
    private Tid tid;
    private Mid mid;
    private Map<Bid, List<Integer>> sets;
    private Optional<String> comment;
}
