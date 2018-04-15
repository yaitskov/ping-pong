package org.dan.ping.pong.app.match.rule.service;

import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.rule.OrderRuleName;
import org.dan.ping.pong.app.match.rule.UidsProvider;
import org.dan.ping.pong.app.match.rule.reason.Reason;
import org.dan.ping.pong.app.match.rule.rules.GroupOrderRule;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface GroupOrderRuleService {
    OrderRuleName getName();
    Optional<Stream<? extends Reason>> score(
            Supplier<Stream<MatchInfo>> matches,
            UidsProvider uids,
            GroupOrderRule rule, GroupRuleParams params);
}