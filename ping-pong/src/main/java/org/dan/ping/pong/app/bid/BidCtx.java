package org.dan.ping.pong.app.bid;

import org.springframework.context.annotation.Import;

@Import({BidDao.class, BidService.class, BidResource.class})
public class BidCtx {
}
