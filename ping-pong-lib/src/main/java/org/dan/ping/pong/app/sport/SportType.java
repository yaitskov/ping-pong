package org.dan.ping.pong.app.sport;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum SportType {
    @JsonProperty("TE")
    Tennis,
    @JsonProperty("PP")
    PingPong
}
