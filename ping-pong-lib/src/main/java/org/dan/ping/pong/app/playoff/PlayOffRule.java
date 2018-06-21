package org.dan.ping.pong.app.playoff;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.dan.ping.pong.app.sport.MatchRules;

import java.util.Optional;

@Getter
@Setter
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
    }
}
