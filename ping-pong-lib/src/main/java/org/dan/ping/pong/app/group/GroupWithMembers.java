package org.dan.ping.pong.app.group;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dan.ping.pong.app.category.CategoryLink;
import org.dan.ping.pong.app.user.UserLink;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupWithMembers {
    private int gid;
    private CategoryLink category;
    private String name;
    private List<UserLink> members;
}
