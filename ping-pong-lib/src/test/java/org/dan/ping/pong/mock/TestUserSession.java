package org.dan.ping.pong.mock;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.bid.Uid;

@Getter
@Setter
@Builder
@EqualsAndHashCode(of = "session")
public class TestUserSession implements SessionAware {
    private String session;
    private Bid bid;
    private Uid uid;
    private String email;
    private String name;
}
