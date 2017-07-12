package org.dan.ping.pong.mock;

import org.dan.ping.pong.app.table.TestTableDao;
import org.dan.ping.pong.sys.ctx.DaoCtx;
import org.dan.ping.pong.sys.db.DbContext;
import org.dan.ping.pong.sys.sadmin.SysAdminCtx;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@Import({DaoCtx.class,
        SysAdminCtx.class,
        DaoEntityGenerator.class,
        RestEntityGenerator.class,
        TestTableDao.class,
        OpenTournamentGenerator.class,
        RestEntityGeneratorWithAdmin.class,
        SysAdminGenerator.class,
        AdminSessionGenerator.class,
        UserSessionGenerator.class,
        DbContext.class})
public class GeneratorCtx {
    @Bean
    public DaoEntityGeneratorWithAdmin daoEntityGeneratorWithAdmin(
            TestAdmin testAdmin, DaoEntityGenerator daoEntityGenerator) {
        return new DaoEntityGeneratorWithAdmin(testAdmin, daoEntityGenerator);
    }
}
