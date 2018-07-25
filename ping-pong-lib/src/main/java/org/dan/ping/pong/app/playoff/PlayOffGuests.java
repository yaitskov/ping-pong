package org.dan.ping.pong.app.playoff;

import static org.dan.ping.pong.app.bid.BidState.Lost;
import static org.dan.ping.pong.app.bid.BidState.Win1;
import static org.dan.ping.pong.app.bid.BidState.Win2;
import static org.dan.ping.pong.app.bid.BidState.Win3;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.app.match.MatchType;

public enum PlayOffGuests {
    /**
     * losers for match final are not counted
     * or participants for match for 3 4 places
     */
    @JsonProperty("LUTS")
    LosersUpToSemifinals {
        public boolean bidStateForConsole(BidState state, MatchType mt) {
            return state == Lost && mt != MatchType.Brnz;
        }
    },
    /**
     * all participants with bid state Lost.
     * Only JustLosers is possible in both PlayOff and Group
     */
    @JsonProperty("JL")
    JustLosers {
        public boolean bidStateForConsole(BidState state, MatchType mt) {
            return state == Lost;
        }
    },
    /**
     * Losers from semifinals
     */
    @JsonProperty("AHF")
    AndHalfFinal {
        public boolean bidStateForConsole(BidState state, MatchType mt) {
            return state == Lost || state == Win3;
        }
    }, // participants fighting for 3 and 4 places
    /**
     * losers and Win3 (have sense for group)
     */
    @JsonProperty("AW3")
    AndWinner3 {
        public boolean bidStateForConsole(BidState state, MatchType mt) {
            return state == Lost || state == Win3;
        }
    },
    /**
     * AndHalfFinal and participant with state Win2
     */
    @JsonProperty("AW2")
    AndWinner2 {
        public boolean bidStateForConsole(BidState state, MatchType mt) {
            return state == Lost || state == Win3 || state == Win2;
        }
    },
    /**
     * just everybody not expelled or quit from the master tournament
     */
    @JsonProperty("AW1")
    AndWinner1 {
        public boolean bidStateForConsole(BidState state, MatchType mt) {
            return state == Lost || state == Win3 || state == Win2 || state == Win1;
        }
    };

    public abstract boolean bidStateForConsole(BidState state, MatchType matchType);
}
