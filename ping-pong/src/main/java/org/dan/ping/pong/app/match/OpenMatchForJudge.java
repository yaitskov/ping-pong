package org.dan.ping.pong.app.match;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.dan.ping.pong.app.table.TableLink;
import org.dan.ping.pong.app.user.UserLink;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OpenMatchForJudge {
    private int mid;
    private int tid;
    private TableLink table;
    private List<UserLink> participants;
    private Instant started;
    private int matchScore;
    private MatchType type;
}
