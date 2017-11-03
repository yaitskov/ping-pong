package org.dan.ping.pong.app.table;

import static org.dan.ping.pong.mock.Generators.genStr;
import static org.junit.Assert.assertEquals;

import org.dan.ping.pong.app.city.CityDao;
import org.dan.ping.pong.app.city.CityLink;
import org.dan.ping.pong.app.city.NewCity;
import org.dan.ping.pong.app.country.CountryDao;
import org.dan.ping.pong.app.country.NewCountry;
import org.dan.ping.pong.app.place.Pid;
import org.dan.ping.pong.app.place.PlaceAddress;
import org.dan.ping.pong.app.place.PlaceDao;
import org.dan.ping.pong.app.place.PlaceDaoServer;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.user.UserDao;
import org.dan.ping.pong.app.user.UserRegRequest;
import org.dan.ping.pong.app.user.UserType;
import org.dan.ping.pong.sys.ctx.PropertiesContext;
import org.dan.ping.pong.sys.db.DbContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {PropertiesContext.class, DbContext.class,
        PlaceDaoServer.class, CityDao.class, CountryDao.class,
        UserDao.class, TableDaoServer.class, TestTableDao.class})
public class TableDaoTest {
    private static final int NUMBER_OF_NEW_TABLES = 10;
    @Inject
    private TableDao tableDao;

    @Inject
    private TestTableDao testTableDao;

    @Inject
    private PlaceDao placeDao;

    @Inject
    private CityDao cityDao;

    @Inject
    private CountryDao countryDao;

    @Inject
    private UserDao userDao;

    @Test
    public void delete() {
        final Uid uid = userDao.register(UserRegRequest.builder().name(genStr()).userType(UserType.User)
                .sessionPart("asdf").build());
        final int countryId = countryDao.create(uid, NewCountry.builder().name(genStr()).build());
        final int cityId = cityDao.create(uid, NewCity.builder().countryId(countryId).name(genStr()).build());
        final Pid pid = placeDao.create(genStr(),
                PlaceAddress.builder()
                        .city(CityLink.builder()
                                .id(cityId).build())
                        .address(genStr())
                        .build());
        tableDao.createTables(pid, NUMBER_OF_NEW_TABLES);
        assertEquals(NUMBER_OF_NEW_TABLES, testTableDao.delete(pid));
        assertEquals(0, testTableDao.delete(pid));
    }
}
