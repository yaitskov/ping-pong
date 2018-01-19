package org.dan.ping.pong.app.country;

import static org.dan.ping.pong.jooq.Tables.COUNTRY;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.Uid;
import org.jooq.DSLContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import javax.inject.Inject;

@Slf4j
public class CountryDao {
    @Inject
    private DSLContext jooq;

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public List<CountryLink> list() {
        return jooq.select(COUNTRY.COUNTRY_ID, COUNTRY.NAME)
                .from(COUNTRY)
                .fetch()
                .map(r -> CountryLink.builder()
                        .name(r.get(COUNTRY.NAME))
                        .id(r.get(COUNTRY.COUNTRY_ID))
                        .build());
    }

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public int countByAuthor(Uid uid) {
        return jooq.selectCount()
                .from(COUNTRY)
                .where(COUNTRY.AUTHOR_ID.eq(uid))
                .fetchOne()
                .value1();
    }

    @Transactional(TRANSACTION_MANAGER)
    public int create(Uid uid, NewCountry newCountry) {
        return jooq.insertInto(COUNTRY, COUNTRY.NAME, COUNTRY.AUTHOR_ID)
                .values(newCountry.getName(), uid)
                .returning(COUNTRY.COUNTRY_ID)
                .fetchOne()
                .get(COUNTRY.COUNTRY_ID);
    }
}
