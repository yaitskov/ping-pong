package org.dan.ping.pong.app.match.rule.service.meta;

import static org.dan.ping.pong.app.match.rule.OrderRuleName._DisambiguationPreview;
import static org.dan.ping.pong.app.match.rule.filter.DisambiguationScope.DISAMBIGUATION_MATCHES;

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
            Supplier<Stream<MatchInfo>> matches, UidsProvider uids,
            GroupOrderRule rule, GroupRuleParams params) {
        params.setDisambiguationMode(DISAMBIGUATION_MATCHES);
        return Optional.empty();
    }
}
