package org.dan.ping.pong.sys.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Error<T> {
    private String id;
    private T message;

    public Error(String id) {
        this(id, null);
    }
}
