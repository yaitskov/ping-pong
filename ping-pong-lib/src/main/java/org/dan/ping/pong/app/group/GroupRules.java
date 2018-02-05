package org.dan.ping.pong.app.group;

import static org.dan.ping.pong.app.group.ConsoleTournament.NO;
import static org.dan.ping.pong.app.group.DisambiguationPolicy.CMP_WIN_AND_LOSE;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Wither;

import java.util.Optional;

@Getter
@Setter
@Wither
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GroupRules {
    private int quits;
    private int groupSize;
    private DisambiguationPolicy disambiguation = CMP_WIN_AND_LOSE;
    private ConsoleTournament console = NO;

    /**
     * Default means {@link GroupSchedule#DEFAULT_SCHEDULE}
     */
    private Optional<GroupSchedule> schedule = Optional.empty();

    public static class GroupRulesBuilder {
        DisambiguationPolicy disambiguation = CMP_WIN_AND_LOSE;
        Optional<GroupSchedule> schedule = Optional.empty();
        ConsoleTournament console = NO;
    }
}
