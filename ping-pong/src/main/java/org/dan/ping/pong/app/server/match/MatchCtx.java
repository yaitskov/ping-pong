package org.dan.ping.pong.app.server.match;

import org.dan.ping.pong.app.server.playoff.PlayOffRuleValidator;
import org.dan.ping.pong.app.server.playoff.PlayOffService;
import org.springframework.context.annotation.Import;

@Import({MatchDao.class, MatchResource.class, MatchService.class,
        PlayOffService.class, PlayOffRuleValidator.class})
public class MatchCtx {
}
