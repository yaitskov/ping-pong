package org.dan.ping.pong.app.match;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.user.UserLink;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayedMatchLink {
    private Mid mid;
    private UserLink opponent;
    private Uid winnerUid;
}
