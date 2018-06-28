package org.dan.ping.pong.app.match;

import static java.util.Collections.emptyList;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dan.ping.pong.app.bid.ParticipantLink;
import org.dan.ping.pong.app.tournament.TournamentProgress;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PlayedMatchList {
    private ParticipantLink participant;
    private TournamentProgress progress;
    private List<PlayedMatchLink> inGroup = emptyList();
    private List<PlayedMatchLink> playOff = emptyList();

    public static class PlayedMatchListBuilder {
        List<PlayedMatchLink> inGroup = emptyList();
        List<PlayedMatchLink> playOff = emptyList();
    }

    public PlayedMatchList merge(PlayedMatchList b) {
        return PlayedMatchList
                .builder()
                .participant(participant)
                .progress(progress.merge(b.progress))
                .inGroup(ImmutableList
                        .<PlayedMatchLink>builder()
                        .addAll(inGroup)
                        .addAll(b.inGroup)
                        .build())
                .playOff(ImmutableList
                        .<PlayedMatchLink>builder()
                        .addAll(playOff)
                        .addAll(b.playOff)
                        .build())
                .build();
    }
}
