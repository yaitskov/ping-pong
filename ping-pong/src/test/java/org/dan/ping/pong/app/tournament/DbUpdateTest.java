package org.dan.ping.pong.app.tournament;


import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.Optional;

public class DbUpdateTest {
    @Test
    public void defaultBuilder() {
        assertEquals(Optional.of(1), DbUpdate.builder().build());
    }
}
