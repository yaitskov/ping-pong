package org.dan.ping.pong.mock;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TestAdmin implements SessionAware {
    private String session;
    private int uid;
    private int said;
}
