package org.dan.ping.pong.app.match;

import static java.util.Collections.emptyList;

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

    public static class OpenMatchForJudgeListBuilder {
        List<OpenMatchForJudge> matches = emptyList();
    }
}
