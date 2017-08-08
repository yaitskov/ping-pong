package org.dan.ping.pong.app.bid;

public enum BidState {
    Want, // User is willing to participate in the tournament
    Paid, // once he paid for the ticket
    Here, // passed check right before the tournament
    Quit {

    }, // resigned during or before tournament on a free will
    Play, // participates right now in a match
    Wait, // wait for place or enemy
    Rest, // temporary not available due timeout
    Lost, // lost tournament battle in a group or playoff game
    Expl, // disqualified out of tournament due rule breaking, etc...
    Win1, // won 1st place
    Win2, // won 2nd place
    Win3 // won 3rd place
}
