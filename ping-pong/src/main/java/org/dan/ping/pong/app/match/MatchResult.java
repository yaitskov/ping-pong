package org.dan.ping.pong.app.match;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.user.UserLink;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MatchResult {
    private Tid tid;
    private int playedSets;
    private MatchScore score;
    private List<UserLink> participants;
    private int minGamesToWin;
    private int disputes;
}
