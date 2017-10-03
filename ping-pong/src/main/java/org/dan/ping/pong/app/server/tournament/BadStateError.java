package org.dan.ping.pong.app.server.tournament;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.dan.ping.pong.sys.error.Error;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class BadStateError<T extends State> extends Error {
    private ErrorCode error;
    private T state;

    BadStateError(ErrorCode code, T state, String message) {
        super(message);
        this.error = code;
        this.state = state;
    }

    public static <T extends State> BadStateError of(T activeState, String message) {
        return new BadStateError(ErrorCode.BadState, activeState, message);
    }
}
