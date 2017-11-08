package org.dan.ping.pong.app.match;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.dan.ping.pong.app.category.CategoryLink;
import org.dan.ping.pong.app.table.TableLink;
import org.dan.ping.pong.app.user.UserLink;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenMatchForWatch {
    private Mid mid;
    private Optional<TableLink> table;
    private List<UserLink> participants;
    private Instant started;
    private MatchType type;
    private List<Integer> score;
    private CategoryLink category;
}
