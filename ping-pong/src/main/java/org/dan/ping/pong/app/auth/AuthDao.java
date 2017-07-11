package org.dan.ping.pong.app.auth;

import static java.util.Optional.ofNullable;
import static ord.dan.ping.pong.jooq.Tables.SESSIONS;
import static ord.dan.ping.pong.jooq.Tables.SESSION_KEY;
import static ord.dan.ping.pong.jooq.Tables.SYS_ADMIN;
import static ord.dan.ping.pong.jooq.Tables.USERS;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;
import static org.dan.ping.pong.sys.error.PiPoEx.notAuthorized;

import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import javax.inject.Inject;

@Slf4j
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

    @Transactional(TRANSACTION_MANAGER)
    public void saveSession(int uid, String token, String deviceInfo) {
        jooq.insertInto(SESSIONS, SESSIONS.TOKEN, SESSIONS.UID, SESSIONS.DEVICE_INFO)
                .values(token, uid, deviceInfo)
                .execute();
    }

    @Transactional(TRANSACTION_MANAGER)
    public int generateToken(String oneTimeToken, String email) {
        final int uid = ofNullable(jooq.select(SESSION_KEY.UID)
                .from(SESSION_KEY)
                .innerJoin(USERS)
                .on(SESSION_KEY.UID.eq(USERS.UID))
                .where(SESSION_KEY.TOKEN.eq(oneTimeToken),
                        USERS.EMAIL.eq(Optional.of(email)))
                .fetchOne())
                .map(r -> r.get(SESSION_KEY.UID))
                .orElseThrow(() -> notAuthorized("Pair or one time token and email doesn't match"));

        jooq.deleteFrom(SESSION_KEY)
                .where(SESSION_KEY.TOKEN.eq(oneTimeToken))
                .execute();
        return uid;
    }

    @Transactional(TRANSACTION_MANAGER)
    public Optional<OneTimeSignInToken> findUidByEmail(String email) {
        return ofNullable(jooq.select(USERS.UID, SESSION_KEY.TOKEN)
                .from(USERS)
                .leftJoin(SESSION_KEY)
                .on(USERS.UID.eq(SESSION_KEY.UID))
                .where(USERS.EMAIL.eq(Optional.of(email)))
                .fetchOne())
                .map(r -> OneTimeSignInToken.builder()
                        .uid(r.get(USERS.UID))
                        .token(ofNullable(r.get(SESSION_KEY.TOKEN)))
                        .build());
    }

    @Transactional(TRANSACTION_MANAGER)
    public void saveOneTimeToken(String oneTimeToken, Integer uid) {
        log.info("Persist one time sign in token for uid {}", uid);
        jooq.insertInto(SESSION_KEY, SESSION_KEY.UID, SESSION_KEY.TOKEN)
                .values(uid, oneTimeToken)
                .execute();
    }
}
