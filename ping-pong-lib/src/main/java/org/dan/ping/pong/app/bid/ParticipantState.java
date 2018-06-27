package org.dan.ping.pong.app.bid;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.dan.ping.pong.app.category.CategoryLink;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantState {
    private ParticipantLink user;
    private CategoryLink category;
    private BidState state;
}
