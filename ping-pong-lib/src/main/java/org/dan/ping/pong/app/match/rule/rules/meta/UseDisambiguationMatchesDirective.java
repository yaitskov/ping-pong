package org.dan.ping.pong.app.match.rule.rules.meta;

import static org.dan.ping.pong.app.match.rule.OrderRuleName.UseDisambiguationMatches;
import static org.dan.ping.pong.app.match.rule.filter.DisambiguationScope.DISAMBIGUATION_MATCHES;
import static org.dan.ping.pong.app.match.rule.filter.MatchOutcomeScope.ALL_MATCHES;
import static org.dan.ping.pong.app.match.rule.filter.MatchParticipantScope.AT_LEAST_ONE;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dan.ping.pong.app.match.rule.OrderRuleName;
import org.dan.ping.pong.app.match.rule.filter.DisambiguationScope;
import org.dan.ping.pong.app.match.rule.filter.MatchOutcomeScope;
import org.dan.ping.pong.app.match.rule.filter.MatchParticipantScope;
import org.dan.ping.pong.app.match.rule.rules.GroupOrderRule;
import org.dan.ping.pong.app.sport.MatchRules;

import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(onConstructor = @__(@JsonIgnore))
public class UseDisambiguationMatchesDirective implements GroupOrderRule {
    private Optional<MatchRules> matchRules = Optional.empty();

    @Override
    public OrderRuleName name() {
        return UseDisambiguationMatches;
    }

    public Optional<DisambiguationScope> disambiguationScope() {
        return Optional.of(DISAMBIGUATION_MATCHES);
    }

    @Override
    @JsonIgnore
    public MatchOutcomeScope getMatchOutcomeScope() {
        return ALL_MATCHES;
    }

    @Override
    public void setMatchOutcomeScope(MatchOutcomeScope scope) {
        // skip
    }

    @Override
    @JsonIgnore
    public MatchParticipantScope getMatchParticipantScope() {
        return AT_LEAST_ONE;
    }

    @Override
    public void setMatchParticipantScope(MatchParticipantScope scope) {
        // skip
    }

    public String toString() {
        return this.getClass().getSimpleName();
    }
}
