package org.dan.ping.pong.app.group;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class GroupInfo {
    private int tid;
    private int gid;
    private int cid;
    private int ordNumber;
}
