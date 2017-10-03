package org.dan.ping.pong.app.server.tournament;

import static org.dan.ping.pong.app.server.castinglots.rank.GroupSplitPolicy.BalancedMix;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Wither;
import org.dan.ping.pong.app.server.castinglots.rank.CastingLotsRule;
import org.dan.ping.pong.app.server.group.GroupRules;
import org.dan.ping.pong.app.server.playoff.PlayOffRule;

import java.util.Optional;

@Getter
@Setter
@Builder
@Wither
@NoArgsConstructor
@AllArgsConstructor
public class TournamentRules {
    private MatchValidationRule match;
    private CastingLotsRule casting;
    private Optional<PlayOffRule> playOff;
    private Optional<GroupRules> group;

    public static class TournamentRulesBuilder {
        CastingLotsRule casting = CastingLotsRule.builder()
                .splitPolicy(BalancedMix)
                .build();
    }
}
