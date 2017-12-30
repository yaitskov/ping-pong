package org.dan.ping.pong.app.tournament;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Wither;
import org.dan.ping.pong.app.castinglots.rank.GroupSplitPolicy;
import org.dan.ping.pong.app.group.GroupRules;
import org.dan.ping.pong.app.match.MatchValidationRule;
import org.dan.ping.pong.app.playoff.PlayOffRule;

import java.util.Optional;

@Getter
@Setter
@Wither
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomConsoleRules {
    private GroupSplitPolicy splitPolicy;
    private MatchValidationRule match;
    private Optional<PlayOffRule> playOff = Optional.empty();
    private Optional<GroupRules> group = Optional.empty();
    private Optional<RewardRules> rewards = Optional.empty();

    public static class CustomConsoleRulesBuilder {
        Optional<PlayOffRule> playOff = Optional.empty();
        Optional<GroupRules> group = Optional.empty();
        Optional<RewardRules> rewards = Optional.empty();
    }
}
