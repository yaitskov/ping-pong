package org.dan.ping.pong.app.auth;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.dan.ping.pong.app.user.UserDao;
import org.dan.ping.pong.app.user.UserInfo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Import({SecureRandom.class, AuthDao.class,
        SysAdminSignInResource.class,
        CheckSysAdminSessionResource.class,
        HelloResource.class,
        CheckUserSessionResource.class,
        AuthService.class})
public class AuthCtx {
    static final String SYS_ADMIN_SESSIONS = "sys_admin_sessions";
    static final String USER_SESSIONS = "user_sessions";

    @Bean(name = SYS_ADMIN_SESSIONS)
    public Cache<String, String> sysAdminSessions() {
        return CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.DAYS)
                .build();
    }

    @Bean(name = USER_SESSIONS)
    public Cache<String, UserInfo> userSessions(UserDao userDao) {
        return CacheBuilder.newBuilder()
                .expireAfterAccess(1, TimeUnit.DAYS)
                .build();
    }
}
