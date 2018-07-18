package org.dan.ping.pong.app.playoff;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum PlayOffGuests {
    /**
     * losers for match final are not counted
     * or participants for match for 3 4 places
     */
    @JsonProperty("LUTS")
    LosersUpToSemifinals,
    /**
     * all participants with bid state Lost.
     * Only JustLosers is possible in both PlayOff and Group
     */
    @JsonProperty("JL")
    JustLosers,
    /**
     * Losers from semifinals
     */
    @JsonProperty("AHF")
    AndHalfFinal, // participants fighting for 3 and 4 places
    /**
     * losers and Win3 (have sense for group)
     */
    @JsonProperty("AW3")
    AndWinner3,
    /**
     * AndHalfFinal and participant with state Win2
     */
    @JsonProperty("AW2")
    AndWinner2,
    /**
     * just everybody not expelled or quit from the master tournament
     */
    @JsonProperty("AW1")
    AndWinner1
}
