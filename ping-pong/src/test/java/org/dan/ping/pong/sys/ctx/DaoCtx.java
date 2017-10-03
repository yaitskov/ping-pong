package org.dan.ping.pong.sys.ctx;

import org.dan.ping.pong.app.server.auth.AuthDao;
import org.dan.ping.pong.app.server.category.CategoryDao;
import org.dan.ping.pong.app.server.city.CityDao;
import org.dan.ping.pong.app.server.country.CountryDao;
import org.dan.ping.pong.app.server.place.PlaceDao;
import org.dan.ping.pong.app.server.table.TableDao;
import org.dan.ping.pong.app.server.tournament.TournamentDao;
import org.dan.ping.pong.app.server.user.UserDao;
import org.springframework.context.annotation.Import;

@Import({AuthDao.class, UserDao.class, PlaceDao.class, CountryDao.class,
        CityDao.class,
        TournamentDao.class, CategoryDao.class, TableDao.class})
public class DaoCtx {}
