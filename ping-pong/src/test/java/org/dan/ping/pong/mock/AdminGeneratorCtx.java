package org.dan.ping.pong.mock;

import static org.dan.ping.pong.mock.AdminSessionGenerator.ADMIN_SESSION;

import org.dan.ping.pong.sys.sadmin.SysAdminCtx;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import javax.inject.Named;

@Import({SysAdminCtx.class,
        RestEntityGeneratorWithAdmin.class,
        SysAdminGenerator.class,
        AdminSessionGenerator.class,
  })
public class AdminGeneratorCtx {
    @Bean
    public DaoEntityGeneratorWithAdmin daoEntityGeneratorWithAdmin(
            @Named(ADMIN_SESSION) TestAdmin testAdmin,
            DaoEntityGenerator daoEntityGenerator) {
        return new DaoEntityGeneratorWithAdmin(testAdmin, daoEntityGenerator);
    }
}
