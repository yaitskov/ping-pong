package org.dan.ping.pong.app.auth;

import static java.security.MessageDigest.getInstance;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.apache.commons.codec.binary.Hex.encodeHexString;
import static org.dan.ping.pong.app.auth.AuthCtx.SYS_ADMIN_SESSIONS;
import static org.dan.ping.pong.app.auth.AuthCtx.USER_SESSIONS;
import static org.dan.ping.pong.app.auth.AuthResource.AUTH_BY_ONE_TIME_TOKEN;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;
import static org.dan.ping.pong.sys.error.PiPoEx.notAuthorized;
import static org.dan.ping.pong.sys.error.PiPoEx.notFound;

import com.google.common.cache.Cache;
import com.google.common.collect.ImmutableMap;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.user.UserDao;
import org.dan.ping.pong.app.user.UserInfo;
import org.dan.ping.pong.sys.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

@Slf4j
public class AuthService {
    public static final int USER_PART_SESSION_LEN = 20;
    public static final String SESSION = "session";
    private static final String SIGN_IN_LINK_PARAM = "sign-in-link";
    private static final String SIGN_IN_LINK_EMAIL_TEMPLATE = "sign-in-link";

    @Inject
    private AuthDao authDao;

    @Inject
    private UserDao userDao;

    @Inject
    private SessionGenerator sessionGenerator;

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
        return sessionGenerator.generate();
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

    @Transactional(TRANSACTION_MANAGER)
    public Authenticated authByOneTimeSession(String oneTimeToken, String email) {
        log.info("Auth attempt by one time toke {} email {}", oneTimeToken, email);
        final int uid = authDao.generateToken(oneTimeToken, email);
        log.info("one time token {} mapped => {}", oneTimeToken, uid);
        final String token = genSession() + genSession();
        authDao.saveSession(uid, token, "to be define");
        return Authenticated.builder()
                .uid(uid)
                .session(token)
                .build();
    }

    @Inject
    private EmailService emailService;

    @Value("${base.site.url}")
    private String baseSiteUrl;

    @Transactional(TRANSACTION_MANAGER)
    public void generateSignInLink(String email) {
        final Optional<OneTimeSignInToken> tokenAndUid = authDao.findUidByEmail(email);
        if (tokenAndUid.isPresent()) {
            final String oneTimeToken = tokenAndUid.get().getToken()
                    .orElseGet(() -> {
                        final String newOneTimeToken  = genSession() + genSession();
                        authDao.saveOneTimeToken(newOneTimeToken, tokenAndUid.get().getUid());
                        return newOneTimeToken;
            });
            emailService.send(email, SIGN_IN_LINK_EMAIL_TEMPLATE,
                    ImmutableMap.of(SIGN_IN_LINK_PARAM,
                            baseSiteUrl + AUTH_BY_ONE_TIME_TOKEN + oneTimeToken
                                    + "/" + email));
        } else {
            log.error("Attempt to issue a sign in link for unknown email {}", email);
        }
    }

    public List<EmailAndToken> emailsWithOneTimeSignInToken() {
        return authDao.emailsWithOneTimeSignInToken();
    }
}
