package org.dan.ping.pong.mock;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.dan.ping.pong.app.bid.Uid;

@Getter
@Builder
@EqualsAndHashCode(of = "session")
public class TestUserSession implements SessionAware {
    private String session;
    private Uid uid;
    private String email;
    private String name;
}
