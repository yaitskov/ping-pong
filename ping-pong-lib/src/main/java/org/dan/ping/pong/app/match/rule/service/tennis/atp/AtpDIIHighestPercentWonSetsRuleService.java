package org.dan.ping.pong.app.match.rule.service.tennis.atp;

import static java.util.Optional.ofNullable;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.AtpDII;
import static org.dan.ping.pong.app.match.rule.reason.DecreasingDoubleScalarReason.ofDoubleD;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.rule.OrderRuleName;
import org.dan.ping.pong.app.match.rule.reason.Reason;
import org.dan.ping.pong.app.match.rule.rules.GroupOrderRule;
import org.dan.ping.pong.app.match.rule.service.GroupOrderRuleService;
import org.dan.ping.pong.app.match.rule.service.GroupRuleParams;
import org.dan.ping.pong.app.match.rule.service.common.LostSetsRuleService;
import org.dan.ping.pong.app.match.rule.service.common.WonSetsRuleService;
import org.dan.ping.pong.app.sport.Sports;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.inject.Inject;

public class AtpDIIHighestPercentWonSetsRuleService implements GroupOrderRuleService {
    @Inject
    private Sports sports;

    @Inject
    private WonSetsRuleService wonSetsRuleService;

    @Inject
    private LostSetsRuleService lostSetsRuleService;

    @Override
    public OrderRuleName getName() {
        return AtpDII;
    }

    @Override
    public Optional<Stream<? extends Reason>> score(
            Supplier<Stream<MatchInfo>> matches,
            Set<Uid> uids,
            GroupOrderRule rule, GroupRuleParams params) {

        final Map<Uid, Integer> uid2WonSets = wonSetsRuleService
                .findUid2WonSets(matches.get(), params);
        final Map<Uid, Integer> uid2LostSets = lostSetsRuleService
                .findUid2WonSets(matches.get(), params);
        return Optional.of(uid2WonSets.entrySet()
                .stream()
                .map((e) -> ofDoubleD(
                        e.getKey(),
                        e.getValue().doubleValue() / (e.getValue()
                                + ofNullable(uid2LostSets.get(e.getKey()))
                                .orElseThrow(() -> internalError(
                                        "no lost " + e.getKey()))),
                        getName()))
                .sorted());
    }
}
