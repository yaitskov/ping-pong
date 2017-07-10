package org.dan.ping.pong.app.auth;

import static java.util.Optional.ofNullable;
import static ord.dan.ping.pong.jooq.Tables.SESSIONS;
import static ord.dan.ping.pong.jooq.Tables.SYS_ADMIN;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;

import org.jooq.DSLContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import javax.inject.Inject;

public class AuthDao {
    @Inject
    private DSLContext jooq;

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public Optional<SysAdmin> findAdminByLogin(String login) {
        return ofNullable(jooq.select(SYS_ADMIN.PASSWORD, SYS_ADMIN.SALT, SYS_ADMIN.SAID)
                .from(SYS_ADMIN)
                .where(SYS_ADMIN.LOGIN.eq(login))
                .fetchOne())
                .map(r3 -> SysAdmin.builder()
                        .login(login)
                        .passwordHash(r3.get(SYS_ADMIN.PASSWORD))
                        .salt(r3.get(SYS_ADMIN.SALT))
                        .id(r3.get(SYS_ADMIN.SAID))
                        .build());
    }

    @Transactional(transactionManager = TRANSACTION_MANAGER)
    public void saveSession(int uid, String token, String deviceInfo) {
        jooq.insertInto(SESSIONS, SESSIONS.TOKEN, SESSIONS.UID, SESSIONS.DEVICE_INFO)
                .values(token, uid, deviceInfo)
                .execute();
    }
}
