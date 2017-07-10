package org.dan.ping.pong.mock;

import org.dan.ping.pong.app.auth.SysAdmin;
import org.dan.ping.pong.sys.sadmin.SysAdminDao;
import org.springframework.context.annotation.Bean;

import javax.inject.Inject;

public class SysAdminGenerator {
    @Inject
    private DaoEntityGenerator daoEntityGenerator;

    @Inject
    private SysAdminDao sysAdminDao;

    @Bean
    public SysAdmin sysAdmin() {
        return sysAdminDao.getById(daoEntityGenerator.genSysAdmin()).get();
    }
}
