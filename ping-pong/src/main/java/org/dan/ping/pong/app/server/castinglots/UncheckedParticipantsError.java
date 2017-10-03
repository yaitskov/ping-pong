package org.dan.ping.pong.app.server.castinglots;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dan.ping.pong.app.server.user.UserLink;
import org.dan.ping.pong.sys.error.Error;

import java.util.List;

@Getter
@NoArgsConstructor
public class UncheckedParticipantsError extends Error {
    private final String error = "uncheckedUsers";
    @Setter
    private List<UserLink> users;

    public UncheckedParticipantsError(List<UserLink> users) {
        super("Unchecked users");
        this.users = users;
    }
}
