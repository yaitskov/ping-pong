package org.dan.ping.pong.app.server.table;

public enum TableState {
    Free,     // free table for playing
    Busy,     // table is used right now for playing
    Archived, // table is not usable for a play (damaged)
    Schedule  // table is locked for a moment to pick up match
}
