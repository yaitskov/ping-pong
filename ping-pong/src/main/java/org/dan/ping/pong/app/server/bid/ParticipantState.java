package org.dan.ping.pong.app.server.bid;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.dan.ping.pong.app.server.category.CategoryInfo;
import org.dan.ping.pong.app.server.user.UserLink;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantState {
    private UserLink user;
    private CategoryInfo category;
    private BidState state;
}
