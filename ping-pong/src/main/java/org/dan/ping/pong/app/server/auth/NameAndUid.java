package org.dan.ping.pong.app.server.auth;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.dan.ping.pong.app.server.user.UserType;

@Getter
@Builder
@ToString
public class NameAndUid {
    private int uid;
    private String name;
    private UserType type;
}
