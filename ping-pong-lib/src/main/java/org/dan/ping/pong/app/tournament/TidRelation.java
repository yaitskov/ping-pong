package org.dan.ping.pong.app.tournament;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dan.ping.pong.app.tournament.console.TournamentRelationType;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TidRelation {
    private Tid tid;
    private TournamentRelationType type;
}
