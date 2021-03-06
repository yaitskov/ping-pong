package org.dan.ping.pong.app.match.rule;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static org.dan.ping.pong.app.match.rule.filter.DisambiguationScope.ORIGIN_MATCHES;
import static org.dan.ping.pong.app.match.rule.filter.MatchOutcomeScope.JUST_NORMALLY_COMPLETE;
import static org.dan.ping.pong.app.match.rule.filter.MatchParticipantScope.AT_LEAST_ONE;
import static org.dan.ping.pong.app.match.rule.service.meta.UseDisambiguationMatchesDirectiveService.matchesInGroup;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.rule.filter.DisambiguationScope;
import org.dan.ping.pong.app.match.rule.filter.FilterMarker;
import org.dan.ping.pong.app.match.rule.filter.MatchOutcomeScope;
import org.dan.ping.pong.app.match.rule.filter.MatchParticipantScope;
import org.dan.ping.pong.app.match.rule.reason.Reason;
import org.dan.ping.pong.app.match.rule.rules.GroupOrderRule;
import org.dan.ping.pong.app.match.rule.service.GroupRuleParams;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Getter
@Setter
@Builder
public class GroupPosition {
    private Set<Bid> competingBids;
    private Optional<Reason> reason = Optional.empty();
    private Optional<GroupOrderRule> rule = Optional.empty();
    private MatchParticipantScope participantScope = AT_LEAST_ONE;
    private DisambiguationScope disambiguationScope = ORIGIN_MATCHES;
    private MatchOutcomeScope outcomeScope = MatchOutcomeScope.ALL_MATCHES;
    private List<MatchInfo> matches;
    private GroupPosition previous;

    public static class GroupPositionBuilder {
        MatchParticipantScope participantScope = AT_LEAST_ONE;
        DisambiguationScope disambiguationScope = ORIGIN_MATCHES;
        MatchOutcomeScope outcomeScope = MatchOutcomeScope.ALL_MATCHES;
        Optional<Reason> reason = Optional.empty();
        Optional<GroupOrderRule> rule = Optional.empty();
    }

    public List<Optional<Reason>> reasonChain() {
        return reasonChain(new LinkedList<>());
    }

    public List<Optional<Reason>> reasonChain(List<Optional<Reason>> chain) {
        if (previous == null) {
            return chain;
        }
        previous.reasonChain(chain);
        chain.add(reason);
        return chain;
    }

    public boolean isSuperOf(GroupPosition sub) {
        return matches != null
                && participantScope.isSuper(sub.participantScope)
                && disambiguationScope.isSuper(sub.disambiguationScope)
                && outcomeScope.isSuper(sub.outcomeScope);
    }

    public boolean isSame(GroupPosition sub) {
        final int aSize = competingBids.size();
        final int bSize = sub.competingBids.size();
        return (aSize + bSize == 0 || aSize == bSize)
                && participantScope == sub.participantScope
                && disambiguationScope == sub.disambiguationScope
                && outcomeScope == sub.outcomeScope;
    }

    public Set<FilterMarker> filters() {
        final Set<FilterMarker> result = new HashSet<>();
        result.add(outcomeScope);
        result.add(disambiguationScope);
        result.add(participantScopeFilter());
        return result;
    }

    public Set<FilterMarker> firstFilters(GroupRuleParams params) {
        final Set<FilterMarker> result = new HashSet<>();
        if (outcomeScope == JUST_NORMALLY_COMPLETE) {
            result.add(outcomeScope);
        }
        if (disambiguationScope != ORIGIN_MATCHES
                || params.getGroupMatches().size() != matchesInGroup(competingBids.size())) {
            result.add(disambiguationScope);
        }
        if (competingBids.size() > 0) {
            result.add(participantScopeFilter());
        }
        return result;
    }

    private FilterMarker participantScopeFilter() {
        switch (participantScope) {
            case AT_LEAST_ONE:
                return s -> s.filter(
                        m -> m.bids().stream().anyMatch(competingBids::contains));
            case BOTH:
                return s -> s.filter(m -> competingBids.containsAll(m.bids()));
            default:
                throw internalError("unknown participant scope "
                        + participantScope);
        }
    }

    public String toString() {
        return format("rule: %s; matches: %s;", rule,
                ofNullable(matches).map(List::size)
                        .map(String::valueOf)
                        .orElse("null"));
    }
}
