package org.dan.ping.pong.mock;

import lombok.Builder;
import lombok.Getter;
import org.dan.ping.pong.app.bid.Uid;

@Getter
@Builder
public class TestAdmin implements SessionAware {
    private String session;
    private Uid uid;
    private int said;
}
