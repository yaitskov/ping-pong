package org.dan.ping.pong.app.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.dan.ping.pong.app.tournament.Uid;

import java.util.Optional;

@Getter
@Builder
@AllArgsConstructor
public class UserInfo {
    private String name;
    private final Uid uid;
    private Optional<String> phone;
    private Optional<String> email;
    private UserType userType;
}
