package org.dan.ping.pong.app.server.group;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class GroupInfo {
    private int gid;
    private int cid;
    private int ordNumber;
    private String label;
}
