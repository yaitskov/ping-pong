package org.dan.ping.pong.app.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.dan.ping.pong.app.bid.Uid;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistration {
    private Uid uid;
    private String session;
    private UserType type;
}
