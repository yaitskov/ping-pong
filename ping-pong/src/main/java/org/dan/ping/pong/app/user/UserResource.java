package org.dan.ping.pong.app.user;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.dan.ping.pong.app.auth.AuthService.SESSION;
import static org.dan.ping.pong.app.user.UserType.User;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.auth.AuthService;
import org.dan.ping.pong.util.time.Clocker;

import javax.inject.Inject;
import javax.jws.soap.SOAPBinding;
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
    static final String USER_INFO_BY_SESSION = "/anonymous/user/info/by/session";

    @Inject
    private UserDao userDao;

    @Inject
    private AuthService authService;

    @POST
    @Path(USER_REGISTER)
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public UserRegistration registerUser(UserRegRequest regRequest,
            @HeaderParam("User-Agent") String agent) {
        regRequest.setUserType(User);
        final int uid = userDao.register(regRequest);
        log.info("Register user [{}] with uid {}",
                regRequest.getName(), uid);
        return UserRegistration.builder()
                .session(authService.authUser(uid, regRequest.getSessionPart(), agent))
                .uid(uid)
                .build();
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
}
