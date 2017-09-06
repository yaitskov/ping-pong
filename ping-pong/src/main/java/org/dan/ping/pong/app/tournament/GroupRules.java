package org.dan.ping.pong.app.tournament;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class GroupRules {
    private int quits;
    private int maxSize;
}
