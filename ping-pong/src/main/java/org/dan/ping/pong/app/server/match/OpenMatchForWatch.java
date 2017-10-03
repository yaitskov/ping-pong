package org.dan.ping.pong.app.server.match;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.dan.ping.pong.app.server.category.CategoryInfo;
import org.dan.ping.pong.app.server.table.TableLink;
import org.dan.ping.pong.app.server.user.UserLink;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenMatchForWatch {
    private int mid;
    private TableLink table;
    private List<UserLink> participants;
    private Instant started;
    private MatchType type;
    private List<Integer> score;
    private CategoryInfo category;
}
