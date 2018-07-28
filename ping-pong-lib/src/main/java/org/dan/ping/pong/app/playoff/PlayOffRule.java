package org.dan.ping.pong.app.playoff;

import static org.dan.ping.pong.app.group.ConsoleTournament.NO;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Wither;
import org.dan.ping.pong.app.group.ConsoleTournament;
import org.dan.ping.pong.app.sport.MatchRules;

import java.util.Optional;

@Getter
@Setter
@Wither
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class PlayOffRule {
    @JsonProperty("tpm")
    private int thirdPlaceMatch;
    private int losings;
    private Optional<MatchRules> match = Optional.empty();
    @JsonProperty("con")
    private ConsoleTournament console = NO;
    @JsonProperty("conParts")
    private Optional<PlayOffGuests> consoleParticipants = Optional.empty();
    @JsonProperty("lrPol")
    private Optional<ConsoleLayersPolicy> layerPolicy = Optional.empty();

    @JsonIgnore
    public boolean is3rdPlaceMatch() {
        return thirdPlaceMatch == 1;
    }

    public static final PlayOffRule L1_3P = PlayOffRule.builder()
            .losings(1)
            .thirdPlaceMatch(1)
            .build();

    public static final PlayOffRule Losing1 = PlayOffRule.builder()
            .losings(1)
            .thirdPlaceMatch(0)
            .build();

    public static final PlayOffRule Losing2 = PlayOffRule.builder()
            .losings(2)
            .thirdPlaceMatch(1) // has no sense
            .build();

    public static class PlayOffRuleBuilder {
        Optional<MatchRules> match = Optional.empty();
        ConsoleTournament console = NO;
        Optional<PlayOffGuests> consoleParticipants = Optional.empty();
        Optional<ConsoleLayersPolicy> layerPolicy = Optional.empty();
    }

    public PlayOffGuests consoleParticipants() {
        return consoleParticipants.orElseThrow(() -> internalError("no console participants"));
    }
}
