package org.dan.ping.pong.app.match;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class Pid {
    private final int pid;

    public String toString() {
        return "pid(" + pid + ")";
    }
}
