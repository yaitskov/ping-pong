package org.dan.ping.pong.app.auth;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class EmailAndToken {
    private String email;
    private String token;
}
