package org.dan.ping.pong.app.place;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ArenaDistributionPolicy {
    /** tables/korts are not managed by the system.
     *  all matches hops from drafting to game state automatically
     */
    NO,
    /** all tables/korts are shared between all participants */
    @JsonProperty("g")
    GLOBAL
}
