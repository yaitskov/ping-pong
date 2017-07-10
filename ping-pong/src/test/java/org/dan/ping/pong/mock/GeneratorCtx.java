package org.dan.ping.pong.mock;

import org.dan.ping.pong.sys.ctx.DaoCtx;
import org.dan.ping.pong.sys.db.DbContext;
import org.dan.ping.pong.sys.sadmin.SysAdminCtx;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import javax.ws.rs.client.Client;

@Import({DaoCtx.class,
        SysAdminCtx.class,
        DaoEntityGenerator.class,
        RestEntityGenerator.class,
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

    @Bean
    public MyLocalRest myLocalRest(Client client) {
        return new MyLocalRest(client);
    }
}
