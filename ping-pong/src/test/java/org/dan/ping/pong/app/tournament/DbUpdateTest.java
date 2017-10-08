package org.dan.ping.pong.app.tournament;


import static org.easymock.EasyMock.createNiceMock;
import static org.junit.Assert.assertNotNull;

import org.dan.ping.pong.sys.db.DbUpdateSql;
import org.jooq.Query;
import org.junit.Test;

public class DbUpdateTest {
    @Test
    public void defaultBuilder() {
        assertNotNull(DbUpdateSql.builder().query(createNiceMock(Query.class)).build().getLogBefore());
    }
}
