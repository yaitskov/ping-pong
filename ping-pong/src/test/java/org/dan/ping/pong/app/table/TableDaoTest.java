package org.dan.ping.pong.app.table;

import static org.dan.ping.pong.mock.Generators.genStr;
import static org.junit.Assert.assertEquals;

import org.dan.ping.pong.app.place.PlaceAddress;
import org.dan.ping.pong.app.place.PlaceDao;
import org.dan.ping.pong.sys.ctx.PropertiesContext;
import org.dan.ping.pong.sys.db.DbContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {PropertiesContext.class, DbContext.class,
        PlaceDao.class, TableDao.class, TestTableDao.class})
public class TableDaoTest {
    private static final int NUMBER_OF_NEW_TABLES = 10;
    @Inject
    private TableDao tableDao;

    @Inject
    private TestTableDao testTableDao;

    @Inject
    private PlaceDao placeDao;

    @Test
    public void delete() {
        final int pid = placeDao.create(genStr(), PlaceAddress.builder().address(genStr()).build());
        tableDao.createTables(pid, NUMBER_OF_NEW_TABLES);
        assertEquals(NUMBER_OF_NEW_TABLES, testTableDao.delete(pid));
        assertEquals(0, testTableDao.delete(pid));
    }
}
