package org.dan.ping.pong.sys.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class Error {
    private String id;
    private String message;

    public Error(String msg) {
        this(UUID.randomUUID().toString(), msg);
    }

    public Error() {
        this(null);
    }
}
