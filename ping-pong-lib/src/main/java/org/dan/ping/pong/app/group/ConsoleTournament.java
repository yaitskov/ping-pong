package org.dan.ping.pong.app.group;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ConsoleTournament {
    NO,
    @JsonProperty("ir")
    INDEPENDENT_RULES
}
