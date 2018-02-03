package org.dan.ping.pong.app.match;

import static java.util.Collections.emptyList;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.dan.ping.pong.app.tournament.TournamentProgress;

import java.util.List;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OpenMatchForJudgeList {
    private List<OpenMatchForJudge> matches = emptyList();
    private TournamentProgress progress;

    public OpenMatchForJudgeList merge(OpenMatchForJudgeList b) {
        return OpenMatchForJudgeList.builder()
                .matches(ImmutableList.<OpenMatchForJudge>builder()
                        .addAll(matches)
                        .addAll(b.getMatches())
                        .build())
                .progress(progress.merge(b.getProgress()))
                .build();
    }

    public static class OpenMatchForJudgeListBuilder {
        List<OpenMatchForJudge> matches = emptyList();
    }
}
