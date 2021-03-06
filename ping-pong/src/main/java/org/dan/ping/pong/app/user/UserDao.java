package org.dan.ping.pong.app.user;

import static java.util.Optional.ofNullable;
import static org.dan.ping.pong.app.user.UserType.Admin;
import static org.dan.ping.pong.jooq.Tables.ADMIN;
import static org.dan.ping.pong.jooq.Tables.USERS;
import static org.dan.ping.pong.jooq.tables.Sessions.SESSIONS;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.sys.error.PiPoEx;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.SelectConditionStep;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import javax.inject.Inject;

@Slf4j
public class UserDao {
    public static final String EMAIL_IS_USED = "Email is already used";

    @Inject
    private DSLContext jooq;

    @Value("${default.user.type}")
    private UserType defaultUserType;

    @Transactional(transactionManager = TRANSACTION_MANAGER)
    public Uid registerDefaultType(UserRegRequest regRequest) throws PiPoEx {
        regRequest.setUserType(defaultUserType);
        return register(regRequest);
    }

    @Transactional(transactionManager = TRANSACTION_MANAGER)
    public Uid register(UserRegRequest regRequest) throws PiPoEx {
        final Uid uid = jooq
                .insertInto(USERS, USERS.NAME, USERS.PHONE,
                        USERS.EMAIL, USERS.TYPE)
                .values(regRequest.getName(),
                        regRequest.getPhone(), regRequest.getEmail(),
                        regRequest.getUserType())
                .returning(USERS.UID)
                .fetchOne()
                .getValue(USERS.UID);
        validateEmailUnique(regRequest.getEmail());
        return uid;
    }

    private void validateEmailUnique(Optional<String> email) {
        if (email.isPresent()) {
            final int matches = jooq.selectCount().from(USERS)
                    .where(USERS.EMAIL.eq(email))
                    .fetchOne()
                    .value1();
            if (matches > 1) {
                throw badRequest(EMAIL_IS_USED);
            }
        }
    }

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public Optional<UserInfo> getUserByUid(Uid uid) {
        return ofNullable(jooq.select(USERS.NAME, USERS.PHONE, USERS.EMAIL, USERS.TYPE)
                .from(USERS)
                .where(USERS.UID.eq(uid))
                .fetchOne())
                .map(r -> UserInfo.builder().uid(uid)
                        .name(r.getValue(USERS.NAME))
                        .email(r.getValue(USERS.EMAIL))
                        .phone(r.getValue(USERS.PHONE))
                        .userType(r.getValue(USERS.TYPE))
                        .build());
    }

    @Transactional(TRANSACTION_MANAGER)
    public Optional<UserInfo> getUserBySession(String token) {
        return ofNullable(jooq.select(USERS.UID, USERS.NAME, USERS.PHONE, USERS.EMAIL, USERS.TYPE)
                .from(SESSIONS).innerJoin(USERS)
                .on(SESSIONS.UID.eq(USERS.UID))
                .where(SESSIONS.TOKEN.eq(token))
                .fetchOne())
                .map(r -> UserInfo.builder().uid(r.getValue(USERS.UID))
                        .name(r.getValue(USERS.NAME))
                        .email(r.getValue(USERS.EMAIL))
                        .phone(r.getValue(USERS.PHONE))
                        .userType(r.getValue(USERS.TYPE))
                        .build());
    }

    @Transactional(TRANSACTION_MANAGER)
    public void promoteToAdmins(int said, Uid uid) {
        log.info("Sys admin {} promoted user {} to admins", said, uid);
        jooq.insertInto(ADMIN, ADMIN.UID, ADMIN.SAID)
                .values(uid, said)
                .onDuplicateKeyIgnore()
                .execute();

        jooq.update(USERS)
                .set(USERS.TYPE, Admin)
                .where(USERS.UID.eq(uid)).execute();
    }

    @Transactional(TRANSACTION_MANAGER)
    public void requestAdminAccess(Uid uid, Instant now) {
        jooq.update(USERS)
                .set(USERS.WANT_ADMIN, Optional.of(now))
                .where(USERS.UID.eq(uid))
                .execute();
    }

    @Transactional(TRANSACTION_MANAGER)
    public void update(UserInfo userInfo, UserProfileUpdate update) {
        jooq.update(USERS)
                .set(USERS.EMAIL, update.getEmail())
                .set(USERS.PHONE, update.getPhone())
                .set(USERS.NAME, update.getName())
                .where(USERS.UID.eq(userInfo.getUid()))
                .execute();
        if (!update.getEmail().equals(userInfo.getEmail())) {
            validateEmailUnique(update.getEmail());
            log.info("User {} changed email from {} to {}",
                    userInfo.getUid(), userInfo.getEmail(), update.getEmail());
        }
    }

    @Transactional(TRANSACTION_MANAGER)
    public Uid registerOffline(Instant now, OfflineUserRegRequest regRequest, Uid adminUid) {
        validateRegOfflineLimits(now, adminUid, 1);
        return registerOfflineNoValidation(regRequest, adminUid);
    }

    @Transactional(TRANSACTION_MANAGER)
    public Uid registerOfflineNoValidation(OfflineUserRegRequest regRequest, Uid adminUid) {
        return jooq
                .insertInto(USERS, USERS.NAME, USERS.TYPE, USERS.REF_UID)
                .values(regRequest.getName(), UserType.OfUsr, adminUid)
                .returning(USERS.UID)
                .fetchOne()
                .getValue(USERS.UID);
    }

    public void validateRegOfflineLimits(Instant now, Uid adminUid, int requested) {
        final int lastDay = count(adminUid, now.minus(1, ChronoUnit.DAYS))
                .fetchOne().value1();
        if (lastDay + requested > 100) {
            throw badRequest("Too many offline users has been registered");
        }
        final int lastWeek = count(adminUid, now.minus(7, ChronoUnit.DAYS))
                .fetchOne().value1();
        if (lastWeek + requested > 300) {
            throw badRequest("Too many offline users has been registered");
        }
        final int lastMonth = count(adminUid, now.minus(31, ChronoUnit.DAYS))
                .fetchOne().value1();
        if (lastMonth + requested > 1000) {
            throw badRequest("Too many offline users has been registered");
        }
    }

    public SelectConditionStep<Record1<Integer>> count(Uid adminUid, Instant oneDay) {
        return jooq.select(USERS.UID.count())
                .from(USERS)
                .where(USERS.REF_UID.eq(adminUid),
                        USERS.CREATED.ge(oneDay));
    }
}
