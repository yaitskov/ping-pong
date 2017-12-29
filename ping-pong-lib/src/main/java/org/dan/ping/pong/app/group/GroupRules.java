package org.dan.ping.pong.app.group;

import static org.dan.ping.pong.app.castinglots.rank.GroupSplitPolicy.BalancedMix;
import static org.dan.ping.pong.app.group.DisambiguationPolicy.CMP_WIN_AND_LOSE;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Wither;
import org.dan.ping.pong.app.castinglots.rank.GroupSplitPolicy;
import org.dan.ping.pong.app.tournament.TournamentRules;

import java.util.Optional;

@Getter
@Setter
@Wither
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupRules {
    private int quits;
    private int groupSize;
    private DisambiguationPolicy disambiguation = CMP_WIN_AND_LOSE;
    private Optional<TournamentRules> console = Optional.empty();

    /**
     * Default means {@link GroupSchedule#DEFAULT_SCHEDULE}
     */
    private Optional<GroupSchedule> schedule = Optional.empty();

    public static class GroupRulesBuilder {
        DisambiguationPolicy disambiguation = CMP_WIN_AND_LOSE;
        Optional<GroupSchedule> schedule = Optional.empty();
        GroupSplitPolicy splitPolicy = BalancedMix;
        Optional<TournamentRules> console = Optional.empty();
    }
}
