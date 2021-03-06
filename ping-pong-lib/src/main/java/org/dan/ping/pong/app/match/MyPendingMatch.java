package org.dan.ping.pong.app.match;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.bid.ParticipantLink;
import org.dan.ping.pong.app.table.TableLink;
import org.dan.ping.pong.app.tournament.Tid;

import java.util.Optional;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class MyPendingMatch {
    private Tid tid;
    private Mid mid;
    private Bid bid;
    private MatchState state;
    private Optional<ParticipantLink> enemy;
    private MatchType matchType;
    private Optional<TableLink> table;
    private MyPendingMatchSport sport;
    private int playedSets;
}
