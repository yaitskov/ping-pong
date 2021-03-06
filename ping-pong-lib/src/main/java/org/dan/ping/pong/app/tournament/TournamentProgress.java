package org.dan.ping.pong.app.tournament;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TournamentProgress {
    private long totalMatches;
    private long leftMatches;

    public TournamentProgress merge(TournamentProgress progress) {
        return TournamentProgress.builder()
                .totalMatches(totalMatches + progress.getTotalMatches())
                .leftMatches(leftMatches + progress.getLeftMatches())
                .build();
    }
}
