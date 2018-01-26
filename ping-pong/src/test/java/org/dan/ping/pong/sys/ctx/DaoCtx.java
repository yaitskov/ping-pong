package org.dan.ping.pong.sys.ctx;

import org.dan.ping.pong.app.auth.AuthDao;
import org.dan.ping.pong.app.category.CategoryDaoMysql;
import org.dan.ping.pong.app.city.CityDao;
import org.dan.ping.pong.app.country.CountryDao;
import org.dan.ping.pong.app.place.PlaceDaoServer;
import org.dan.ping.pong.app.table.TableDaoServer;
import org.dan.ping.pong.app.tournament.TournamentDaoMySql;
import org.dan.ping.pong.app.user.UserDao;
import org.springframework.context.annotation.Import;

@Import({AuthDao.class, UserDao.class, PlaceDaoServer.class, CountryDao.class,
        CityDao.class,
        TournamentDaoMySql.class, CategoryDaoMysql.class, TableDaoServer.class})
public class DaoCtx {}
