package org.dan.ping.pong.app.tournament;

import static org.dan.ping.pong.app.castinglots.rank.GroupSplitPolicy.BalancedMix;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Wither;
import org.dan.ping.pong.app.castinglots.rank.CastingLotsRule;
import org.dan.ping.pong.app.group.GroupRules;
import org.dan.ping.pong.app.match.MatchValidationRule;
import org.dan.ping.pong.app.playoff.PlayOffRule;

import java.util.Optional;

@Getter
@Setter
@Builder
@Wither
@NoArgsConstructor
@AllArgsConstructor
public class TournamentRules {
    public static final int FIRST_VERSION = 1;
    private int version = FIRST_VERSION;
    private MatchValidationRule match;
    private CastingLotsRule casting;
    private Optional<PlayOffRule> playOff;
    private Optional<GroupRules> group;

    public static class TournamentRulesBuilder {
        int version = FIRST_VERSION;
        CastingLotsRule casting = CastingLotsRule.builder()
                .splitPolicy(BalancedMix)
                .build();
    }
}
