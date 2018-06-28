package org.dan.ping.pong.app.match;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.dan.ping.pong.app.bid.ParticipantLink;
import org.dan.ping.pong.app.category.CategoryLink;
import org.dan.ping.pong.app.table.TableLink;

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
    private List<ParticipantLink> participants;
    private Instant started;
    private MatchType type;
    private List<Integer> score;
    private CategoryLink category;
}
