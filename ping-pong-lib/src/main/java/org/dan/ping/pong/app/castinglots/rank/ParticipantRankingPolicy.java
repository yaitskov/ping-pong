package org.dan.ping.pong.app.castinglots.rank;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ParticipantRankingPolicy {
    @JsonProperty("su")
    SignUp,
    @JsonProperty("pr")
    ProvidedRating,
    @JsonProperty("h")
    History,
    @JsonProperty("m")
    Manual,
    @JsonProperty("mo")
    MasterOutcome
}
