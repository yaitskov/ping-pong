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

@Getter
@Setter
@Builder
@Wither
@NoArgsConstructor
@AllArgsConstructor
public class TournamentRules {
    private MatchValidationRule match;
    private CastingLotsRule casting;
    private GroupRules group;
    private int prizeWinningPlaces = 3;
    private int thirdPlaceMatch;

    public static class TournamentRulesBuilder {
        int prizeWinningPlaces = 3;
        CastingLotsRule casting = CastingLotsRule.builder()
                .splitPolicy(BalancedMix)
                .build();
    }
}
