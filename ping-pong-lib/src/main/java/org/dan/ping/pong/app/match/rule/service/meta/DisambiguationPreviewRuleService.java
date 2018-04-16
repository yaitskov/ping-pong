package org.dan.ping.pong.app.match.rule.service.meta;

import static org.dan.ping.pong.app.match.rule.OrderRuleName._DisambiguationPreview;

import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.rule.OrderRuleName;
import org.dan.ping.pong.app.match.rule.UidsProvider;
import org.dan.ping.pong.app.match.rule.reason.Reason;
import org.dan.ping.pong.app.match.rule.rules.GroupOrderRule;
import org.dan.ping.pong.app.match.rule.service.GroupOrderRuleService;
import org.dan.ping.pong.app.match.rule.service.GroupRuleParams;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class DisambiguationPreviewRuleService implements GroupOrderRuleService {
    @Override
    public OrderRuleName getName() {
        return _DisambiguationPreview;
    }

    @Override
    public Optional<Stream<? extends Reason>> score(
            Supplier<Stream<MatchInfo>> _matches, UidsProvider _uids,
            GroupOrderRule _rule, GroupRuleParams _params) {
        return Optional.empty();
    }
}
