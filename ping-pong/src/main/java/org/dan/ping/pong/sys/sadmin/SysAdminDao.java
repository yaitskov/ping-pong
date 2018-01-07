package org.dan.ping.pong.sys.sadmin;

import static java.util.Optional.ofNullable;
import static org.dan.ping.pong.jooq.tables.SysAdmin.SYS_ADMIN;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.jooq.Tables;
import org.dan.ping.pong.app.auth.SysAdmin;
import org.jooq.DSLContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import javax.inject.Inject;

@Slf4j
public class SysAdminDao {
    @Inject
    private DSLContext jooq;

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public Optional<SysAdmin> getById(int said) {
        return ofNullable(jooq
                .select(SYS_ADMIN.PASSWORD, SYS_ADMIN.SALT, SYS_ADMIN.LOGIN)
                .from(SYS_ADMIN)
                .where(SYS_ADMIN.SAID.eq(said))
                .fetchOne())
                .map(r3 -> SysAdmin.builder()
                        .login(r3.get(SYS_ADMIN.LOGIN))
                        .passwordHash(r3.get(SYS_ADMIN.PASSWORD))
                        .salt(r3.get(SYS_ADMIN.SALT))
                        .id(said)
                        .build());
    }

    @Inject
    private PasswordHasher hasher;

    @Transactional(TRANSACTION_MANAGER)
    public int create(String login, String password, String salt) {
        log.info("Create sys admin {}", login);
        return jooq.insertInto(Tables.SYS_ADMIN, Tables.SYS_ADMIN.LOGIN,
                Tables.SYS_ADMIN.PASSWORD, Tables.SYS_ADMIN.SALT)
                .values(login, hasher.apply(password, salt), salt)
                .returning(Tables.SYS_ADMIN.SAID)
                .fetchOne().getSaid();
    }
}
