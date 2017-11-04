package org.dan.ping.pong.app.match;

import org.dan.ping.pong.app.match.dispute.MatchDisputeCtx;
import org.dan.ping.pong.app.playoff.PlayOffRuleValidator;
import org.dan.ping.pong.app.playoff.PlayOffService;
import org.springframework.context.annotation.Import;

@Import({MatchDaoServer.class, MatchResource.class, MatchService.class,
        MatchDisputeCtx.class,
        PlayOffService.class, PlayOffRuleValidator.class})
public class MatchCtx {
}
