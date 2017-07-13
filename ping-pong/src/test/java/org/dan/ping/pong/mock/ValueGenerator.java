package org.dan.ping.pong.mock;

import java.util.UUID;

public class ValueGenerator {
    public String genName() {
        return UUID.randomUUID().toString();
    }

    public String genName(int len) {
        return genName().substring(0, len);
    }
}
