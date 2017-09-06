package org.dan.ping.pong.app.match;

import org.dan.ping.pong.app.playoff.PlayOffService;
import org.springframework.context.annotation.Import;

@Import({MatchDao.class, MatchResource.class, MatchService.class, PlayOffService.class})
public class MatchCtx {
}
