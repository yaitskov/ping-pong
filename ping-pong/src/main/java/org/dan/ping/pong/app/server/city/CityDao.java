package org.dan.ping.pong.app.server.city;

import static ord.dan.ping.pong.jooq.tables.City.CITY;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;

import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import javax.inject.Inject;

@Slf4j
public class CityDao {
    @Inject
    private DSLContext jooq;

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public List<CityLink> listByCountry(int countryId) {
        return jooq.select(CITY.CITY_ID, CITY.NAME)
                .from(CITY)
                .where(CITY.COUNTRY_ID.eq(countryId))
                .fetch()
                .map(r -> CityLink.builder()
                        .name(r.get(CITY.NAME))
                        .id(r.get(CITY.CITY_ID))
                        .build());
    }

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public int countByAuthor(int uid) {
        return jooq.selectCount()
                .from(CITY)
                .where(CITY.AUTHOR_ID.eq(uid))
                .fetchOne()
                .value1();
    }

    @Transactional(TRANSACTION_MANAGER)
    public int create(int uid, NewCity newCity) {
        return jooq.insertInto(CITY, CITY.NAME, CITY.AUTHOR_ID, CITY.COUNTRY_ID)
                .values(newCity.getName(), uid, newCity.getCountryId())
                .returning(CITY.CITY_ID)
                .fetchOne()
                .get(CITY.CITY_ID);
    }
}
