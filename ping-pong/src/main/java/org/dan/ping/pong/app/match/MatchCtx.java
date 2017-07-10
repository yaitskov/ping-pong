package org.dan.ping.pong.app.match;

import org.springframework.context.annotation.Import;

@Import({MatchDao.class, MatchResource.class, MatchService.class})
public class MatchCtx {
}
