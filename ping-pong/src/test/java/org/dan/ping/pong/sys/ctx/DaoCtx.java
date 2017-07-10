package org.dan.ping.pong.sys.ctx;

import org.dan.ping.pong.app.auth.AuthDao;
import org.dan.ping.pong.app.category.CategoryDao;
import org.dan.ping.pong.app.place.PlaceDao;
import org.dan.ping.pong.app.table.TableDao;
import org.dan.ping.pong.app.tournament.TournamentDao;
import org.dan.ping.pong.app.user.UserDao;
import org.dan.ping.pong.sys.sadmin.SysAdminDao;
import org.springframework.context.annotation.Import;

@Import({AuthDao.class, UserDao.class, PlaceDao.class,
        TournamentDao.class, SysAdminDao.class, CategoryDao.class,
        TableDao.class})
public class DaoCtx {}
