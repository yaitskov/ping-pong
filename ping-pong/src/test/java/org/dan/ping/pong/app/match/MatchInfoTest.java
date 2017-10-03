package org.dan.ping.pong.app.match;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.Optional;

public class MatchInfoTest {
    @Test
    public void winnerMidDefaultEmpty() {
        assertEquals(Optional.empty(), MatchInfo.builder().build().getWinnerMid());
    }
}
