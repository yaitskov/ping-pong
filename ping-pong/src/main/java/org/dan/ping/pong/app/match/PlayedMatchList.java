package org.dan.ping.pong.app.match;

import static java.util.Collections.emptyList;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dan.ping.pong.app.tournament.TournamentProgress;
import org.dan.ping.pong.app.user.UserLink;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PlayedMatchList {
    private UserLink participant;
    private TournamentProgress progress;
    private List<PlayedMatchLink> inGroup = emptyList();
    private List<PlayedMatchLink> playOff = emptyList();

    public static class PlayedMatchListBuilder {
        List<PlayedMatchLink> inGroup = emptyList();
        List<PlayedMatchLink> playOff = emptyList();
    }
}
