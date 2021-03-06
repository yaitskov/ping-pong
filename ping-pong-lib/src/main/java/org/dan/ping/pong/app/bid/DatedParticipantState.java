package org.dan.ping.pong.app.bid;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.dan.ping.pong.app.category.CategoryLink;
import org.dan.ping.pong.app.user.UserLink;

import java.time.Instant;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DatedParticipantState {
    private UserLink user;
    private CategoryLink category;
    private BidState state;
    private Instant enlistedAt;
}
