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
import org.dan.ping.pong.app.place.PlaceRules;
import org.dan.ping.pong.app.playoff.PlayOffRule;

import java.util.Optional;

@Getter
@Setter
@Builder
@Wither
@NoArgsConstructor
@AllArgsConstructor
public class TournamentRules {
    public static final int FIRST_VERSION = 2;
    private int version = FIRST_VERSION;
    private MatchValidationRule match;
    private CastingLotsRule casting;
    private Optional<PlayOffRule> playOff = Optional.empty();
    private Optional<GroupRules> group = Optional.empty();
    private Optional<PlaceRules> place = Optional.empty();
    private Optional<RewardRules> rewards = Optional.empty();

    public static class TournamentRulesBuilder {
        int version = FIRST_VERSION;
        Optional<PlayOffRule> playOff = Optional.empty();
        Optional<GroupRules> group = Optional.empty();
        Optional<RewardRules> rewards = Optional.empty();
        Optional<PlaceRules> place = Optional.empty();
        CastingLotsRule casting = CastingLotsRule.builder()
                .splitPolicy(BalancedMix)
                .build();
    }
}
