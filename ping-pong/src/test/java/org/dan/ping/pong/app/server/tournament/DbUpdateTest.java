package org.dan.ping.pong.app.server.tournament;


import static org.easymock.EasyMock.createNiceMock;
import static org.junit.Assert.assertNotNull;

import org.jooq.Query;
import org.junit.Test;

public class DbUpdateTest {
    @Test
    public void defaultBuilder() {
        assertNotNull(DbUpdate.builder().query(createNiceMock(Query.class)).build().getLogBefore());
    }
}
