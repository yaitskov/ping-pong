package org.dan.ping.pong.app.server.group;

import static org.dan.ping.pong.app.server.castinglots.rank.GroupSplitPolicy.BalancedMix;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Wither;
import org.dan.ping.pong.app.server.castinglots.rank.GroupSplitPolicy;

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

    /**
     * Default means {@link GroupSchedule#DEFAULT_SCHEDULE}
     */
    private Optional<GroupSchedule> schedule = Optional.empty();

    public static class GroupRulesBuilder {
        private Optional<GroupSchedule> schedule = Optional.empty();
        private GroupSplitPolicy splitPolicy = BalancedMix;
    }
}
