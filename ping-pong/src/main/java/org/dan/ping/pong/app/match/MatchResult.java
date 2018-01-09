package org.dan.ping.pong.app.match;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dan.ping.pong.app.category.CategoryLink;
import org.dan.ping.pong.app.group.GroupLink;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.user.UserLink;
import org.dan.ping.pong.app.user.UserRole;

import java.util.List;
import java.util.Optional;

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
    private MyPendingMatchSport sport;
    private int disputes;
    private MatchState state;
    private MatchType type;
    private Optional<GroupLink> group = Optional.empty();
    private CategoryLink category;
    private UserRole role;

    public static class MatchResultBuilder {
        Optional<GroupLink> group = Optional.empty();
    }
}
