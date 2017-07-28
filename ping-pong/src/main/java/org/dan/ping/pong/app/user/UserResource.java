package org.dan.ping.pong.app.user;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.dan.ping.pong.app.user.UserType.User;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.auth.AuthService;

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
    public UserRegistration doPost(UserRegRequest regRequest,
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
}
