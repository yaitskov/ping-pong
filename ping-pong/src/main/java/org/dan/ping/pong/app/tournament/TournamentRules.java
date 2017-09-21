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
import org.dan.ping.pong.app.playoff.PlayOffRule;

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
    private int prizeWinningPlaces = 3;

    public static class TournamentRulesBuilder {
        int prizeWinningPlaces = 3;
        CastingLotsRule casting = CastingLotsRule.builder()
                .splitPolicy(BalancedMix)
                .build();
    }
}
