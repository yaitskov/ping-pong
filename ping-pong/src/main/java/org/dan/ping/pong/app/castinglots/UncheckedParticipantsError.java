package org.dan.ping.pong.app.castinglots;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dan.ping.pong.app.bid.ParticipantLink;
import org.dan.ping.pong.sys.error.Error;

import java.util.List;

@Getter
@NoArgsConstructor
public class UncheckedParticipantsError extends Error {
    private final String error = "uncheckedUsers";
    @Setter
    private List<ParticipantLink> users;

    public UncheckedParticipantsError(List<ParticipantLink> users) {
        super("Unchecked users");
        this.users = users;
    }
}
