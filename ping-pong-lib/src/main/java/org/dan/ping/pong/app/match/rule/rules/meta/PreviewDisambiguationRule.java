package org.dan.ping.pong.app.match.rule.rules.meta;

import static org.dan.ping.pong.app.match.rule.OrderRuleName._DisambiguationPreview;

import org.dan.ping.pong.app.match.rule.OrderRuleName;

public class PreviewDisambiguationRule extends CountDisambiguationMatchesRule {
    @Override
    public OrderRuleName name() {
        return _DisambiguationPreview;
    }
}
