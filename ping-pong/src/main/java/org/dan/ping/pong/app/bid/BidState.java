package org.dan.ping.pong.app.bid;

public enum BidState {
    Want, // User is willing to participate in the tournament
    Paid, // once he paid for the ticket
    Here, // passed check right before the tournament
    Play, // participates right now in a match
    Rest {
        public int score() {
            return 4;
        }
    }, // temporary not available due timeout
    Wait {
        public int score() {
            return 4;
        }
    }, // wait for place or enemy
    Expl {
        public int score() {
            return 10;
        }
    }, // disqualified out of tournament due rule breaking, etc...
    Quit {
        public int score() {
            return 4;
        }
    }, // resigned during or before tournament on a free will
    Lost {
        public int score() {
            return 4;
        }
    }, // lost tournament battle in a group or playoff game
    Win3 {
        public int score() {
            return 3;
        }
    }, // won 3rd place
    Win2 {
        public int score() {
            return 2;
        }
    }, // won 2nd place
    Win1 {
        public int score() {
            return 1;
        }
    }; // won 1st place

    public int score() {
        return 100;
    }
}
