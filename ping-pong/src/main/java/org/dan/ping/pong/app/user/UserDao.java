package org.dan.ping.pong.app.user;

import static java.util.Optional.ofNullable;
import static ord.dan.ping.pong.jooq.Tables.ADMIN;
import static ord.dan.ping.pong.jooq.Tables.USERS;
import static ord.dan.ping.pong.jooq.tables.Sessions.SESSIONS;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.sys.error.PiPoEx;
import org.jooq.DSLContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import javax.inject.Inject;

@Slf4j
public class UserDao {
    public static final String EMAIL_IS_USED = "Email is already used";

    @Inject
    private DSLContext jooq;

    @Transactional(transactionManager = TRANSACTION_MANAGER)
    public int register(UserRegRequest regRequest) throws PiPoEx {
        final int uid = jooq
                .insertInto(USERS, USERS.NAME, USERS.PHONE, USERS.EMAIL)
                .values(regRequest.getName(),
                        regRequest.getPhone(), regRequest.getEmail())
                .returning(USERS.UID)
                .fetchOne()
                .getValue(USERS.UID);
        if (regRequest.getEmail().isPresent()) {
            final int matches = jooq.selectCount().from(USERS)
                    .where(USERS.EMAIL.eq(regRequest.getEmail()))
                    .fetchOne()
                    .value1();
            if (matches > 1) {
                throw badRequest(EMAIL_IS_USED);
            }
        }
        return uid;
    }

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public Optional<UserInfo> getUserByUid(int uid) {
        return ofNullable(jooq.select(USERS.NAME, USERS.PHONE, USERS.EMAIL)
                .from(USERS)
                .where(USERS.UID.eq(uid))
                .fetchOne())
                .map(r -> UserInfo.builder().uid(uid)
                        .name(r.getValue(USERS.NAME))
                        .email(r.getValue(USERS.EMAIL))
                        .phone(r.getValue(USERS.PHONE)).build());
    }

    @Transactional(transactionManager = TRANSACTION_MANAGER)
    public Optional<UserInfo> getUserBySession(String token) {
        return ofNullable(jooq.select(USERS.UID, USERS.NAME, USERS.PHONE, USERS.EMAIL)
                .from(SESSIONS).innerJoin(USERS)
                .on(SESSIONS.UID.eq(USERS.UID))
                .where(SESSIONS.TOKEN.eq(token))
                .fetchOne())
                .map(r -> UserInfo.builder().uid(r.getValue(USERS.UID))
                        .name(r.getValue(USERS.NAME))
                        .email(r.getValue(USERS.EMAIL))
                        .phone(r.getValue(USERS.PHONE))
                        .build());
    }

    @Transactional(transactionManager = TRANSACTION_MANAGER)
    public void promoteToAdmins(int said, int uid) {
        log.info("Sys admin {} promoted user {} to admins", said, uid);
        jooq.insertInto(ADMIN, ADMIN.UID, ADMIN.SAID)
                .values(uid, said)
                .onDuplicateKeyIgnore()
                .execute();
    }
}
