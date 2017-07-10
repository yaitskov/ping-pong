package org.dan.ping.pong.app.auth;

import static java.security.MessageDigest.getInstance;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.apache.commons.codec.binary.Hex.encodeHexString;
import static org.dan.ping.pong.app.auth.AuthCtx.SYS_ADMIN_SESSIONS;
import static org.dan.ping.pong.app.auth.AuthCtx.USER_SESSIONS;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;
import static org.dan.ping.pong.sys.error.PiPoEx.notAuthorized;
import static org.dan.ping.pong.sys.error.PiPoEx.notFound;

import com.google.common.cache.Cache;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.user.UserDao;
import org.dan.ping.pong.app.user.UserInfo;

import java.security.SecureRandom;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

@Slf4j
public class AuthService {
    public static final int USER_PART_SESSION_LEN = 20;
    public static final String SESSION = "session";

    @Inject
    private AuthDao authDao;

    @Inject
    private UserDao userDao;

    @Inject
    private SecureRandom random;

    @Inject
    @Named(SYS_ADMIN_SESSIONS)
    private Cache<String, String> sessionLogin;

    @Inject
    @Named(USER_SESSIONS)
    private Cache<String, UserInfo> userSessions;

    public Optional<String> authSysAdmin(SysAdminAuth auth) {
        return authDao.findAdminByLogin(auth.getLogin())
                .filter(sysAdmin -> hashPassword(sysAdmin.getSalt(), auth.getPassword())
                        .equals(sysAdmin.getPasswordHash()))
                .map(sysAdmin -> {
                    final String session = genSession();
                    log.info("Assign session {} to sys admin {}", session, auth.getLogin());
                    sessionLogin.put(session, auth.getLogin());
                    return session;
                });
    }

    private String genSession() {
        return encodeHexString(random.generateSeed(10));
    }

    @SneakyThrows
    private String hashPassword(String salt , String password) {
        return encodeHexString(getInstance("SHA-1")
                .digest((password + salt).getBytes()));
    }

    public String authUser(int uid, String userSessionPart, String deviceInfo) {
        if (userSessionPart.length() != USER_PART_SESSION_LEN) {
            throw badRequest("user session part should have length 20");
        }
        final UserInfo userInfo = userDao.getUserByUid(uid)
                .orElseThrow(() -> notFound("user " + uid + " is not used"));
        final String fullSession = genSession() + userSessionPart;
        authDao.saveSession(uid, fullSession, deviceInfo);
        userSessions.put(fullSession, userInfo);
        return fullSession;
    }

    public Optional<UserInfo> userInfoBySessionQuite(final String session) {
        if (session == null) {
            return empty();
        }
        final UserInfo cachedUserInfo = userSessions.getIfPresent(session);
        if (cachedUserInfo == null) {
            final Optional<UserInfo> dbUserInfo = userDao.getUserBySession(session);
            log.info("Load session {} user {} from db", session, dbUserInfo.map(UserInfo::getUid));
            dbUserInfo.ifPresent(userInfo -> userSessions.put(session, userInfo));
            return dbUserInfo;
        }
        return Optional.of(cachedUserInfo);
    }

    public UserInfo userInfoBySession(final String session) {
        return ofNullable(userSessions.getIfPresent(
                ofNullable(session).orElseThrow(() -> notAuthorized("No session header"))))
                .orElseGet(() -> {
                    final UserInfo userInfo = userDao.getUserBySession(session)
                            .orElseThrow(() -> notAuthorized("token " + session + " is not valid"));
                    userSessions.put(session, userInfo);
                    return userInfo;
                });
    }
}
