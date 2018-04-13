package org.dan.ping.pong.app.playoff;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.tournament.TournamentResultEntry;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
public class PlayOffResultEntries {
   private List<TournamentResultEntry> entries;
   private Set<Uid> playOffUids;
}
