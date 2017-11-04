package org.dan.ping.pong.app.match.dispute;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.match.Mid;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DisputeMemState {
    private DisputeId did;
    private Mid mid;
    private Uid plaintiff;
    private Optional<Uid> judge;
    private DisputeStatus status;
    private Instant created;
    private Optional<Instant> resolvedAt;
    private Map<Uid, List<Integer>> proposedScore;
    private Optional<String> plaintiffComment;
    private Optional<String> judgeComment;
}
