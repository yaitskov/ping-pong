package org.dan.ping.pong.app.country;

import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import javax.inject.Inject;

public class CountryService {
    @Inject
    private CountryDao countryDao;

    public List<CountryLink> list() {
        return countryDao.list();
    }

    @Transactional(TRANSACTION_MANAGER)
    public int create(int uid, NewCountry newCountry) {
        final int hasDefined = countryDao.countByAuthor(uid);
        if (hasDefined > 3) {
            throw badRequest("You reached the limited of custom countries");
        }
        return countryDao.create(uid, newCountry);
    }
}
