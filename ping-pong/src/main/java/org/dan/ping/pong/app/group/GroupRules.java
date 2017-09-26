package org.dan.ping.pong.app.group;

import static org.dan.ping.pong.app.castinglots.rank.GroupSplitPolicy.BalancedMix;

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
    private int maxSize;
    /**
     * Default means {@link GroupSchedule#DEFAULT_SCHEDULE}
     */
    private Optional<GroupSchedule> schedule = Optional.empty();

    public static class GroupRulesBuilder {
        private Optional<GroupSchedule> schedule = Optional.empty();
        private GroupSplitPolicy splitPolicy = BalancedMix;
    }
}
