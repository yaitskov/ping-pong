package org.dan.ping.pong.app.bid;

import org.springframework.context.annotation.Import;

@Import({BidDaoServer.class, BidService.class, BidResource.class})
public class BidCtx {
}
