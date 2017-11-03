package org.dan.ping.pong.app.match;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.dan.ping.pong.app.group.GroupLink;
import org.dan.ping.pong.app.user.UserLink;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CompleteMatch {
    private Mid mid;
    private Optional<GroupLink> group;
    private List<UserLink> participants;
    private Instant started;
    private Instant ended;
    private List<Integer> score;
    private MatchType type;
}
