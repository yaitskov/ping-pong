package org.dan.ping.pong.app.city;

import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import javax.inject.Inject;

public class CityService {
    @Inject
    private CityDao cityDao;

    public List<CityLink> listByCountry(int countryId) {
        return cityDao.listByCountry(countryId);
    }

    @Transactional(TRANSACTION_MANAGER)
    public int create(int uid, NewCity newCity) {
        final int hasDefined = cityDao.countByAuthor(uid);
        if (hasDefined > 3) {
            throw badRequest("You reached the limited of custom cities");
        }
        return cityDao.create(uid, newCity);
    }
}
