package org.dan.ping.pong.app.match.rule.service.tennis.atp;

import static java.util.Optional.ofNullable;
import static org.dan.ping.pong.app.match.rule.reason.DecreasingDoubleScalarReason.nanAs0;
import static org.dan.ping.pong.app.match.rule.reason.DecreasingDoubleScalarReason.ofDoubleD;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.rule.reason.Reason;
import org.dan.ping.pong.app.match.rule.rules.GroupOrderRule;
import org.dan.ping.pong.app.match.rule.service.GroupOrderRuleService;
import org.dan.ping.pong.app.match.rule.service.GroupRuleParams;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

public abstract class BasePercentRuleService implements GroupOrderRuleService {
    @Override
    public Optional<Stream<? extends Reason>> score(
            Supplier<Stream<MatchInfo>> matches,
            Set<Bid> bids,
            GroupOrderRule rule, GroupRuleParams params) {
        final Map<Bid, Integer> uid2WonSets = findBid2WonSets(matches.get(), params);
        final Map<Bid, Integer> uid2LostSets = findBid2LostSets(matches.get(), params);
        return Optional.of(uid2WonSets.entrySet()
                .stream()
                .map((e) -> ofDoubleD(
                        e.getKey(),
                        nanAs0(e.getValue().doubleValue() / (e.getValue()
                                + ofNullable(uid2LostSets.get(e.getKey()))
                                .orElseThrow(() -> internalError(
                                        "no lost " + e.getKey())))),
                        getName()))
                .sorted());
    }

    protected abstract Map<Bid,Integer> findBid2LostSets(
            Stream<MatchInfo> matchInfoStream, GroupRuleParams params);


    protected abstract Map<Bid,Integer> findBid2WonSets(
            Stream<MatchInfo> matchInfoStream, GroupRuleParams params);
}
