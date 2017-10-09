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

    /**
     * Default means {@link GroupSchedule#DEFAULT_SCHEDULE}
     */
    private Optional<GroupSchedule> schedule = Optional.empty();

    public static class GroupRulesBuilder {
        private DisambiguationPolicy disambiguation = CMP_WIN_AND_LOSE;
        private Optional<GroupSchedule> schedule = Optional.empty();
        private GroupSplitPolicy splitPolicy = BalancedMix;
    }
}
