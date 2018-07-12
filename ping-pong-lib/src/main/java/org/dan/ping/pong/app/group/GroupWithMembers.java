package org.dan.ping.pong.app.group;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dan.ping.pong.app.bid.ParticipantLink;
import org.dan.ping.pong.app.category.CategoryLink;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupWithMembers {
    private Gid gid;
    private CategoryLink category;
    private String name;
    private List<ParticipantLink> members;
}
