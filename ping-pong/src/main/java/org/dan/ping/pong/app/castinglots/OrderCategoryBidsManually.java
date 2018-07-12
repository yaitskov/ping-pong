package org.dan.ping.pong.app.castinglots;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.category.Cid;
import org.dan.ping.pong.app.tournament.Tid;

import java.util.List;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OrderCategoryBidsManually {
    private Tid tid;
    private Cid cid;
    private List<Bid> bids;
}
