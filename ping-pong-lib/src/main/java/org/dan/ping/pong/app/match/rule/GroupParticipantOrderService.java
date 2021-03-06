package org.dan.ping.pong.app.match.rule;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.dan.ping.pong.app.match.rule.GroupParticipantOrder.orderOf;
import static org.dan.ping.pong.app.match.rule.filter.GroupMatchFilter.applyFilters;
import static org.dan.ping.pong.app.match.rule.filter.MatchParticipantScope.AT_LEAST_ONE;
import static org.dan.ping.pong.app.match.rule.reason.InfoReason.notApplicableRule;
import static org.dan.ping.pong.app.match.rule.service.GroupOrderRuleServiceCtx.GROUP_ORDER_SERVICES_BY_RULE_NAME;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.rule.filter.FilterMarker;
import org.dan.ping.pong.app.match.rule.reason.Reason;
import org.dan.ping.pong.app.match.rule.rules.GroupOrderRule;
import org.dan.ping.pong.app.match.rule.service.GroupOrderRuleService;
import org.dan.ping.pong.app.match.rule.service.GroupRuleParams;
import org.dan.ping.pong.util.collection.CounterInt;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Resource;

public class GroupParticipantOrderService {
    @Resource(name = GROUP_ORDER_SERVICES_BY_RULE_NAME)
    private Map<OrderRuleName, GroupOrderRuleService> groupOrderRuleServiceMap;

    public GroupParticipantOrder findOrder(GroupRuleParams params) {
        final List<GroupOrderRule> orderRules = params.getOrderRules();
        final GroupParticipantOrder result = orderOf(params.getBids());
        final List<GroupOrderRuleService> services = services(orderRules);

        for (int i = 0; i < services.size(); ++i) {
            final GroupOrderRuleService service = services.get(i);
            final GroupOrderRule rule = orderRules.get(i);
            orderStep(rule, result, service, params);
            if (result.unambiguous()) {
                break;
            }
        }

        return result;
    }

    private List<GroupOrderRuleService> services(List<GroupOrderRule> orderRules) {
        return orderRules.stream()
                .map(rule -> ofNullable(groupOrderRuleServiceMap.get(rule.name()))
                        .orElseThrow(() -> internalError(
                                "No service for rule " + rule.name())))
                .collect(Collectors.toList());
    }

    private Supplier<Stream<MatchInfo>> matches(GroupRuleParams params,
            GroupPosition ambiPos) {
        if (ambiPos.getMatches() == null) {
            return () -> {
                findMatches(ambiPos, params, ambiPos.getPrevious());
                return ambiPos.getMatches().stream();
            };
        } else {
            return () -> ambiPos.getMatches().stream();
        }
    }

    private void findMatches(GroupPosition required,
            GroupRuleParams params, GroupPosition sup) {
        if (sup == null) {
            final Set<FilterMarker> filters = required.firstFilters(params);
            if (filters.isEmpty()) {
                required.setMatches(params.getGroupMatches());
            } else {
                required.setMatches(
                        applyFilters(params.getGroupMatches().stream(), filters)
                                .collect(Collectors.toList()));
            }
        } else if (sup.isSuperOf(required)) {
            if (sup.isSame(required)) {
                required.setMatches(sup.getMatches());
            } else {
                required.setMatches(
                        applyFilters(sup.getMatches().stream(),
                                required.filters())
                                .collect(Collectors.toList()));
            }
        } else {
            findMatches(required, params, sup.getPrevious());
        }
    }

    private static class GroupSplitLoop {
        Reason lastWonLost;
        Set<Bid> bids = new HashSet<>();
    }

    private void orderStep(GroupOrderRule rule,
            GroupParticipantOrder order,
            GroupOrderRuleService service,
            GroupRuleParams params) {
        final Set<GroupPositionIdx> positions = order.getAmbiguousPositions();
        order.setAmbiguousPositions(new HashSet<>());
        for (GroupPositionIdx ambiPosI : positions) {
            final GroupPosition parentGroupPos = order.getPositions().remove(ambiPosI);
            final GroupPosition ambiPos = GroupPosition.builder()
                    .previous(parentGroupPos)
                    .reason(empty())
                    .competingBids(parentGroupPos.getCompetingBids())
                    .build();
            setScopes(ambiPos, rule, params);
            order.getAmbiguousPositions().remove(ambiPosI);
            final Supplier<Stream<MatchInfo>> matchesSupplier = matches(params, ambiPos);
            final Optional<Stream<? extends Reason>> score = service.score(
                    matchesSupplier, ambiPos.getCompetingBids(), rule, params)
                    .map(s -> filterOutPairUids(s, rule, ambiPos));
            if (!score.isPresent()) {
                order.getPositions().put(ambiPosI, ambiPos);
                ambiPos.setReason(Optional.of(notApplicableRule(rule.name())));
                order.getAmbiguousPositions().add(ambiPosI);
                continue;
            }
            splitUidsIntoCompetingGroups(order, ambiPosI, ambiPos, score);
        }
    }

    private Stream<? extends Reason> filterOutPairUids(Stream<? extends Reason> s,
            GroupOrderRule rule, GroupPosition ambiPos) {
        if (rule.getMatchParticipantScope() == AT_LEAST_ONE
                && !ambiPos.getCompetingBids().isEmpty()) {
            return s.filter(wl -> ambiPos.getCompetingBids()
                    .contains(wl.getBid()));
        }
        return s;
    }

    private void splitUidsIntoCompetingGroups(GroupParticipantOrder order,
            GroupPositionIdx ambiPosI, GroupPosition ambiPos,
            Optional<Stream<? extends Reason>> score) {
        final GroupSplitLoop loop = new GroupSplitLoop();
        final CounterInt counterInt = new CounterInt();
        final GroupPositionIdx[] newGroupPosIdx = new GroupPositionIdx[] { ambiPosI };
        score.get().forEachOrdered(wl -> {
            if (loop.lastWonLost != null
                    && !wl.equalsWithoutUid(loop.lastWonLost)) {
                flush(order, newGroupPosIdx[0], ambiPos, loop);
                newGroupPosIdx[0] = ambiPosI.plus(counterInt.toInt());
                loop.bids.clear();
            }
            ambiPos.getCompetingBids().remove(wl.getBid());
            loop.bids.add(wl.getBid());
            counterInt.inc();
            loop.lastWonLost = wl;
        });
        if (!loop.bids.isEmpty()) {
            flush(order, newGroupPosIdx[0], ambiPos, loop);
        }
        ensureUidsWithoutMatchesFlushed(order, ambiPosI, ambiPos, loop, counterInt);
    }

    private void ensureUidsWithoutMatchesFlushed(GroupParticipantOrder order,
            GroupPositionIdx ambiPosI, GroupPosition ambiPos, GroupSplitLoop loop,
            CounterInt counterInt) {
        switch (ambiPos.getCompetingBids().size()) {
            case 0:
                return;
            case 1:
                loop.lastWonLost = notApplicableRule(
                        ambiPos.getCompetingBids().stream().findAny().get(),
                        ambiPos.getRule().get().name());
                break;
            default:
                loop.lastWonLost = notApplicableRule(ambiPos.getRule().get().name());
        }
        loop.bids = ambiPos.getCompetingBids();
        flush(order, ambiPosI.plus(counterInt.toInt()), ambiPos, loop);
    }

    private void setScopes(GroupPosition ambiPos,
            GroupOrderRule rule, GroupRuleParams params) {
        ambiPos.setRule(Optional.of(rule));
        ambiPos.setOutcomeScope(rule.getMatchOutcomeScope());
        ambiPos.setParticipantScope(rule.getMatchParticipantScope());
        rule.disambiguationScope().ifPresent(params::setDisambiguationMode);
        ambiPos.setDisambiguationScope(params.getDisambiguationMode());
    }

    private void flush(GroupParticipantOrder order, GroupPositionIdx ambiPosI,
            GroupPosition ambiPos, GroupSplitLoop loop) {
        final Set<Bid> competingBids = new HashSet<>(loop.bids);
        final GroupPosition gp = GroupPosition.builder()
                .competingBids(competingBids)
                .previous(ambiPos.getPrevious())
                .matches(ambiPos.getMatches())
                .reason(Optional.of(loop.lastWonLost))
                .rule(ambiPos.getRule())
                .outcomeScope(ambiPos.getOutcomeScope())
                .participantScope(ambiPos.getParticipantScope())
                .disambiguationScope(ambiPos.getDisambiguationScope())
                .build();
        final GroupPosition lostGp = order.getPositions().put(ambiPosI, gp);
        if (lostGp != null) {
            throw internalError("Group position [" + lostGp
                    + "] is overridden by [" + gp + "] at idx = " + ambiPosI);
        }
        if (loop.bids.size() > 1) {
            order.getAmbiguousPositions().add(ambiPosI);
        }
    }
}
