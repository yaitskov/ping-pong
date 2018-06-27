package org.dan.ping.pong.app.user;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.dan.ping.pong.app.auth.AuthCtx.USER_SESSIONS;
import static org.dan.ping.pong.app.auth.AuthService.SESSION;
import static org.dan.ping.pong.app.user.UserType.Admin;
import static org.dan.ping.pong.app.user.UserType.User;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;

import com.google.common.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.auth.AuthService;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.util.time.Clocker;

import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

@Slf4j
@Path("/")
public class UserResource {
    static final String USER_REGISTER = "/anonymous/user/register";
    public static final String OFFLINE_USER_REGISTER = "/anonymous/offline-user/register";
    static final String USER_INFO_BY_SESSION = "/anonymous/user/info/by/session";

    @Inject
    private UserDao userDao;

    @Inject
    private AuthService authService;

    @POST
    @Path(USER_REGISTER)
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public UserRegistration registerUser(
            @Valid UserRegRequest regRequest,
            @HeaderParam("User-Agent") String agent) {
        final Uid uid = userDao.registerDefaultType(regRequest);
        log.info("Register user [{}] with uid {}", regRequest.getName(), uid);
        return UserRegistration.builder()
                .session(authService.authUser(uid, regRequest.getSessionPart(), agent))
                .uid(uid)
                .type(regRequest.getUserType())
                .build();
    }

    @POST
    @Path(OFFLINE_USER_REGISTER)
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Uid registerOffineUser(
            @HeaderParam(SESSION) String session,
            @Valid OfflineUserRegRequest regRequest) {
        final UserInfo userInfo = authService.userInfoBySession(session);
        if (userInfo.getUserType() != Admin) {
            throw badRequest("Must be admin");
        }
        final Uid uid = userDao.registerOffline(clocker.get(), regRequest, userInfo.getUid());
        log.info("Register user [{}] with uid {}", regRequest.getName(), uid);
        return uid;
    }

    @GET
    @Path(USER_INFO_BY_SESSION + "/{session}")
    @Produces(APPLICATION_JSON)
    public UserInfo userInfoBySession(@PathParam("session") String session) {
        return authService.userInfoBySession(session);
    }

    @Inject
    private Clocker clocker;

    @POST
    @Path("/user/request-admin-access")
    public void requestAdminAccess(@HeaderParam(SESSION) String session) {
        final UserInfo userInfo = authService.userInfoBySession(session);
        if (userInfo.getUserType() != User) {
            log.info("Admin access request from {} rejected due {}",
                    userInfo.getUid(), userInfo.getUserType());
            return;
        }
        log.info("User {} requested admin access", userInfo.getUid());
        userDao.requestAdminAccess(userInfo.getUid(), clocker.get());
    }

    @Inject
    @Named(USER_SESSIONS)
    private Cache<String, UserInfo> userSessions;

    @POST
    @Path("/user/profile/update")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public void registerUser(
            UserProfileUpdate update,
            @HeaderParam(SESSION) String session) {
        final UserInfo userInfo = authService.userInfoBySession(session);
        userDao.update(userInfo, update);
        userSessions.asMap()
                .forEach((s, data) -> {
                    if (data.getEmail().equals(userInfo.getEmail())) {
                        log.info("Invalidate session {} in cache", s);
                        userSessions.invalidate(s);
                    }
                });
    }
}
