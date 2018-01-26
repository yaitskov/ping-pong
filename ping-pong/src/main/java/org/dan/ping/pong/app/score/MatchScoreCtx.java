package org.dan.ping.pong.app.score;

import org.springframework.context.annotation.Import;

@Import({MatchScoreDaoMysql.class})
public class MatchScoreCtx {
}
