package org.dan.ping.pong.app.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Optional;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegRequest {
    @Max(80)
    @Min(3)
    private String name;
    @Max(40)
    private Optional<String> email;
    private Optional<String> phone;
    private String sessionPart;
    @Setter
    private UserType userType;

    public static class UserRegRequestBuilder {
        Optional<String> email = Optional.empty();
        Optional<String> phone = Optional.empty();
        UserType userType = UserType.User;
    }
}
