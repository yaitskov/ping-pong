package org.dan.ping.pong.app.playoff;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ConsoleLayersPolicy {
    @JsonProperty("ml")
    MergeLayers,
    @JsonProperty("il")
    IndependentLayers
}
