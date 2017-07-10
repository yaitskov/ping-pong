package org.dan.ping.pong.app.group;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class GroupLink {
    private int gid;
    private String label;
}
