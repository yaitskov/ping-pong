package org.dan.ping.pong.app.group;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.dan.ping.pong.app.category.Cid;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class GroupInfo {
    private Gid gid;
    private Cid cid;
    private int ordNumber;
    private String label;

    public GroupLink toLink() {
        return GroupLink.builder().label(label).gid(gid).build();
    }

    public static GroupInfo groupOf(Gid gid, Cid cid, int ordNumber, String label) {
        return GroupInfo.builder()
                .gid(gid)
                .cid(cid)
                .ordNumber(ordNumber)
                .label(label)
                .build();
    }
}
