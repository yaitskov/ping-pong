package org.dan.ping.pong.app.auth;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.dan.ping.pong.app.tournament.Uid;
import org.dan.ping.pong.app.user.UserType;

@Getter
@Builder
@ToString
public class NameAndUid {
    private Uid uid;
    private String name;
    private UserType type;
}
