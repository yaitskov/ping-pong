package org.dan.ping.pong.app.match;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.dan.ping.pong.app.table.TableLink;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.user.UserLink;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OpenMatchForJudge {
    private Mid mid;
    private Tid tid;
    private Optional<TableLink> table;
    private List<UserLink> participants;
    private Instant started;
    private int minGamesToWin;
    private int playedSets;
    private MatchType matchType;
}
