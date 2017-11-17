package org.dan.ping.pong.mock.simulator.imerative;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.mock.simulator.Player;

import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class BidStatesDesc {
    private final Map<Player, BidState> special = new HashMap<>();
    private final BidState rest;

    public static BidStatesDesc restState(BidState state) {
        return new BidStatesDesc(state);
    }

    public BidStatesDesc bid(Player p, BidState state){
        special.put(p, state);
        return this;
    }
}
