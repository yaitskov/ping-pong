package org.dan.ping.pong.mock;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.mock.simulator.Player;
import org.dan.ping.pong.mock.simulator.PlayerCategory;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Builder
@EqualsAndHashCode(of = "session")
public class TestUserSession implements SessionAware {
    private String session;
    private Uid uid;
    private final Player player;
    private Map<PlayerCategory, Bid> catBid = new HashMap<>();
    private String email;
    private String name;

    public static class TestUserSessionBuilder {
        Map<PlayerCategory, Bid> catBid = new HashMap<>();
    }
}
