package org.dan.ping.pong.app.match;

import static java.util.Collections.emptyList;
import static java.util.Optional.empty;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.dan.ping.pong.app.table.TableLink;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.user.UserLink;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OpenMatchForJudge {
    private Mid mid;
    private Tid tid;
    private Optional<TableLink> table = empty();
    private List<UserLink> participants = emptyList();
    private Optional<Instant> started = empty();
    private int minGamesToWin;
    private int minAdvanceInGames;
    private int playedSets;
    private MatchType matchType;

    public static class OpenMatchForJudgeBuilder {
        Optional<Instant> started = empty();
        Optional<TableLink> table = empty();
        List<UserLink> participants = emptyList();
    }
}
