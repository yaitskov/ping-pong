package org.dan.ping.pong.app.match;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.dan.ping.pong.app.table.TableLink;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.user.UserLink;

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
    private MatchState state;
    private Optional<UserLink> enemy;
    private MatchType matchType;
    private Optional<TableLink> table;
    private int minGamesToWin;
    private int playedSets;
}
