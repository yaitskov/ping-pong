package org.dan.ping.pong.sys.warmup;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.dan.ping.pong.app.auth.AuthService.SESSION;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.auth.AuthService;
import org.dan.ping.pong.app.user.UserInfo;

import javax.inject.Inject;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Slf4j
@Path("/")
@Produces(APPLICATION_JSON)
public class WarmUpResource {
    @Inject
    private WarmUpService warmUpService;
    @Inject
    private AuthService authService;

    @POST
    public int warmUp(
            @HeaderParam(SESSION) String session,
            WarmUpRequest request) {
        final UserInfo user = authService.userInfoBySession(session);
        return warmUpService.warmUp(user, request);
    }
}
