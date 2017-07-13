package org.dan.ping.pong.mock;

import org.dan.ping.pong.sys.sadmin.SysAdminCtx;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@Import({SysAdminCtx.class,
        RestEntityGeneratorWithAdmin.class,
        SysAdminGenerator.class,
        AdminSessionGenerator.class,
  })
public class AdminGeneratorCtx {
    @Bean
    public DaoEntityGeneratorWithAdmin daoEntityGeneratorWithAdmin(
            TestAdmin testAdmin, DaoEntityGenerator daoEntityGenerator) {
        return new DaoEntityGeneratorWithAdmin(testAdmin, daoEntityGenerator);
    }
}
