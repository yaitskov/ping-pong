package org.dan.ping.pong.mock;

import org.dan.ping.pong.app.tournament.Uid;
import org.springframework.context.annotation.Bean;

import javax.inject.Inject;

public class AdminSessionGenerator {
    public static final String ADMIN_SESSION = "adminSession";

    @Inject
    private DaoEntityGenerator daoEntityGenerator;

    @Inject
    private SysAdminGenerator sysAdminGenerator;

    @Bean(name = ADMIN_SESSION)
    public TestAdmin generate() {
        final int said = sysAdminGenerator.genSysAdmin();
        final Uid adminId = daoEntityGenerator.genAdmin(said);
        return TestAdmin.builder()
                .said(said)
                .uid(adminId)
                .session(daoEntityGenerator.genUserSession(adminId))
                .build();
    }
}
