package org.dan.ping.pong.app.match.rule;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.dan.ping.pong.app.match.rule.GroupParticipantOrder.orderOf;
import static org.dan.ping.pong.app.match.rule.filter.GroupMatchFilter.applyFilters;
import static org.dan.ping.pong.app.match.rule.filter.MatchParticipantScope.BOTH;
import static org.dan.ping.pong.app.match.rule.service.GroupOrderRuleServiceCtx.GROUP_ORDER_SERVICES_BY_RULE_NAME;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import org.dan.ping.pong.app.bid.Uid;
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
        final GroupParticipantOrder result = orderOf(params.getUids());
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
        Set<Uid> uids;
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
                    .competingUids(parentGroupPos.getCompetingUids())
                    .build();
            setScopes(ambiPos, rule, params);
            order.getAmbiguousPositions().remove(ambiPosI);
            final Supplier<Stream<MatchInfo>> matchesSupplier = matches(params, ambiPos);
            final Optional<Stream<? extends Reason>> score = service.score(
                    matchesSupplier,
                    new UidsProvider(ambiPos.getCompetingUids(), matchesSupplier),
                    rule, params)
                    .map(s -> (rule.getMatchParticipantScope() == BOTH
                            || ambiPos.getCompetingUids().isEmpty())
                            ? s
                            : s.filter(wl -> ambiPos.getCompetingUids().contains(wl.getUid())));
            if (!score.isPresent()) {
                order.getPositions().put(ambiPosI, ambiPos);
                order.getAmbiguousPositions().add(ambiPosI);
                continue;
            }
            splitUidsIntoCompetingGroups(order, ambiPosI, ambiPos, score);
        }
    }

    private void splitUidsIntoCompetingGroups(GroupParticipantOrder order,
            GroupPositionIdx ambiPosI, GroupPosition ambiPos,
            Optional<Stream<? extends Reason>> score) {
        final GroupSplitLoop loop = new GroupSplitLoop();
        final CounterInt counterInt = new CounterInt();
        score.get().forEachOrdered(wl -> {
            if (loop.lastWonLost != null
                    && wl.compareTo(loop.lastWonLost) != 0) {
                flush(order, ambiPosI, ambiPos, loop, counterInt);
                loop.uids.clear();
                loop.uids.add(wl.getUid());
            }
            counterInt.inc();
            loop.lastWonLost = wl;
        });
        if (loop.uids.isEmpty()) {
            return ;
        }
        flush(order, ambiPosI, ambiPos, loop, counterInt);
    }

    private void setScopes(GroupPosition ambiPos,
            GroupOrderRule rule, GroupRuleParams params) {
        ambiPos.setOutcomeScope(rule.getMatchOutcomeScope());
        ambiPos.setParticipantScope(rule.getMatchParticipantScope());
        ambiPos.setDisambiguationScope(params.getDisambiguationMode());
    }

    private void flush(GroupParticipantOrder order, GroupPositionIdx ambiPosI,
            GroupPosition ambiPos, GroupSplitLoop loop, CounterInt counterInt) {
        final GroupPositionIdx newGroupPosIdx = ambiPosI.plus(counterInt.toInt());
        final Set<Uid> competingUids = new HashSet<>(loop.uids);
        order.getPositions().put(newGroupPosIdx,
                GroupPosition.builder()
                        .competingUids(competingUids)
                        .previous(ambiPos)
                        .reason(Optional.of(loop.lastWonLost))
                        .build());
        if (loop.uids.size() > 1) {
            order.getAmbiguousPositions().add(newGroupPosIdx);
        }
    }
}
