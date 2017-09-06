package org.dan.ping.pong.app.tournament;


import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class DbUpdateTest {
    @Test
    public void defaultBuilder() {
        assertNotNull(DbUpdate.builder().build().getLogBefore());
    }
}
