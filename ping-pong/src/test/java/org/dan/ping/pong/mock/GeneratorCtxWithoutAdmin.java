package org.dan.ping.pong.mock;

import org.dan.ping.pong.app.table.TestTableDao;
import org.dan.ping.pong.sys.ctx.DaoCtx;
import org.dan.ping.pong.sys.db.DbContext;
import org.springframework.context.annotation.Import;

@Import({DaoCtx.class,
        DaoEntityGenerator.class,
        ValueGenerator.class,
        RestEntityGenerator.class,
        TestTableDao.class,
        UserSessionGenerator.class,
        DbContext.class})
public class GeneratorCtxWithoutAdmin {
}
