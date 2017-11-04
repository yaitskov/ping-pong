package org.dan.ping.pong.app.match.dispute;

import org.springframework.context.annotation.Import;

@Import({MatchDisputeResource.class, MatchDisputeService.class, MatchDisputeDaoServer.class})
public class MatchDisputeCtx {
}
