package org.dan.ping.pong.mock;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode(of = "session")
public class TestUserSession implements SessionAware {
    private String session;
    private int uid;
    private String email;
}
