package org.dan.ping.pong.app.group;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class GroupInfo {
    private int gid;
    private int cid;
    private int ordNumber;
    private String label;

    public GroupLink toLink() {
        return GroupLink.builder().label(label).gid(gid).build();
    }
}
