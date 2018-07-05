package org.dan.ping.pong.app.tournament;

import static org.dan.ping.pong.app.castinglots.rank.GroupSplitPolicy.BalancedMix;
import static org.dan.ping.pong.app.group.ConsoleTournament.NO;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Wither;
import org.dan.ping.pong.app.castinglots.rank.CastingLotsRule;
import org.dan.ping.pong.app.group.GroupRules;
import org.dan.ping.pong.app.place.PlaceRules;
import org.dan.ping.pong.app.playoff.PlayOffRule;
import org.dan.ping.pong.app.sport.MatchRules;

import java.util.Optional;

@Getter
@Setter
@Builder
@Wither
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TournamentRules {
    public static final int FIRST_VERSION = 2;
    @JsonProperty("v")
    private int version = FIRST_VERSION;
    private MatchRules match;
    private CastingLotsRule casting;
    private Optional<PlayOffRule> playOff = Optional.empty();
    private Optional<GroupRules> group = Optional.empty();
    private Optional<PlaceRules> place = Optional.empty();
    private Optional<RewardRules> rewards = Optional.empty();
    private Optional<EnlistPolicy> enlist = Optional.empty();

    public static class TournamentRulesBuilder {
        int version = FIRST_VERSION;
        Optional<EnlistPolicy> enlist = Optional.empty();
        Optional<PlayOffRule> playOff = Optional.empty();
        Optional<GroupRules> group = Optional.empty();
        Optional<RewardRules> rewards = Optional.empty();
        Optional<PlaceRules> place = Optional.empty();
        CastingLotsRule casting = CastingLotsRule.builder()
                .splitPolicy(BalancedMix)
                .build();
    }

    public boolean consoleP() {
        return group.map(GroupRules::getConsole).orElse(NO) != NO;
    }

    public GroupRules group() {
        return group.orElseThrow(() -> internalError("tournament without groups"));
    }
}
