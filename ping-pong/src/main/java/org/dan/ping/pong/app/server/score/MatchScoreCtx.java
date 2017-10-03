package org.dan.ping.pong.app.server.score;

import org.springframework.context.annotation.Import;

@Import({MatchScoreDao.class})
public class MatchScoreCtx {
}
