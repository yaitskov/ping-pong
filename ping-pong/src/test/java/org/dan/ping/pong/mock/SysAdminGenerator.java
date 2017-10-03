package org.dan.ping.pong.mock;

import static org.dan.ping.pong.mock.Generators.genStr;

import org.dan.ping.pong.app.server.auth.SysAdmin;
import org.dan.ping.pong.sys.sadmin.SysAdminDao;
import org.springframework.context.annotation.Bean;

import javax.inject.Inject;

public class SysAdminGenerator {
    public static final String SYS_ADMIN_TEST_PASSWORD = "1";

    @Inject
    private SysAdminDao sysAdminDao;

    @Bean
    public SysAdmin sysAdmin() {
        return sysAdminDao.getById(genSysAdmin()).get();
    }

    private int genSysAdmin(String login) {
        return sysAdminDao.create(login, SYS_ADMIN_TEST_PASSWORD, "salt");
    }

    public int genSysAdmin() {
        return genSysAdmin(genStr());
    }
}
