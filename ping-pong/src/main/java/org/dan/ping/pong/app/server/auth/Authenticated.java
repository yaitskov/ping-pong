package org.dan.ping.pong.app.server.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.dan.ping.pong.app.server.user.UserType;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Authenticated {
    private int uid;
    private String session;
    private String name;
    private UserType type;
}
