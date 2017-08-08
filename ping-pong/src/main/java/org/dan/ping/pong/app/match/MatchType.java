package org.dan.ping.pong.app.match;

import static org.dan.ping.pong.app.bid.BidState.Expl;
import static org.dan.ping.pong.app.bid.BidState.Quit;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import org.dan.ping.pong.app.bid.BidState;

public enum MatchType {
    Grup {
        @Override
        public BidState getWinnerState() {
            throw internalError("group match");
        }

        @Override
        public BidState getLoserState(BidState state) {
            throw internalError("group match");
        }
    },
    POff {
        @Override
        public BidState getWinnerState() {
            throw internalError("ordinary play off match");
        }
    },
    Brnz {
        @Override
        public BidState getWinnerState() {
            return BidState.Win3;
        }
    },
    Gold {
        @Override
        public BidState getWinnerState() {
            return BidState.Win1;
        }

        @Override
        public BidState getLoserState(BidState state) {
            if (state == Quit) {
                return BidState.Win2;
            }
            return super.getLoserState(state);
        }
    };

    public abstract BidState getWinnerState();

    public BidState getLoserState(BidState state) {
        switch (state) {
            case Expl:
                return Expl;
            case Quit:
                return Quit;
            default:
                return BidState.Lost;
        }
    }
}
