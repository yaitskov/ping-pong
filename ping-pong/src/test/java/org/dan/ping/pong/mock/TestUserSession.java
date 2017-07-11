package org.dan.ping.pong.mock;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TestUserSession implements SessionAware {
    private String session;
    private int uid;
    private String email;
}
