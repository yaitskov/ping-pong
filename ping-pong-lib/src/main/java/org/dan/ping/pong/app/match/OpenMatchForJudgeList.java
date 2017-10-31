package org.dan.ping.pong.app.match;

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
@AllArgsConstructor
public class OpenMatchForJudgeList {
    private List<OpenMatchForJudge> matches;
    private TournamentProgress progress;
}
