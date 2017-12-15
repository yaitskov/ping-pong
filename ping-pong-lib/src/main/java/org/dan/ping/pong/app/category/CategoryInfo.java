package org.dan.ping.pong.app.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dan.ping.pong.app.user.UserLink;
import org.dan.ping.pong.app.user.UserRole;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryInfo {
    private CategoryLink link;
    private UserRole role;
    private List<UserLink> users;
}
