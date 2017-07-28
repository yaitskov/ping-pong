package org.dan.ping.pong.app.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Optional;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegRequest {
    private String name;
    private Optional<String> email;
    private Optional<String> phone;
    private String sessionPart;
    @Setter
    private UserType userType;
}
