package org.dan.ping.pong.app.bid;

import org.dan.ping.pong.app.bid.result.BidResultService;
import org.springframework.context.annotation.Import;

@Import({BidDaoServer.class, BidService.class,
        BidResultService.class, BidResource.class})
public class BidCtx {
}
