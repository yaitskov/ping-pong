package org.dan.ping.pong.app.tournament;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BadStateError<T extends State> {
    private Error error;
    private T state;

    public static <T extends State> BadStateError of(T activeState) {
        return BadStateError.builder()
                .error(Error.BadState)
                .state(activeState)
                .build();
    }
}
