package org.dan.ping.pong.sys.warmup;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.dan.ping.pong.app.auth.AuthService.SESSION;
import static org.dan.ping.pong.app.tournament.ParticipantMemState.FILLER_LOSER_UID;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.auth.AuthService;
import org.dan.ping.pong.app.user.UserInfo;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Slf4j
@Path("/")
@Produces(APPLICATION_JSON)
public class WarmUpResource {
    public static final String WARM_UP = "/warm/up";

    @Inject
    private WarmUpService warmUpService;
    @Inject
    private AuthService authService;

    @POST
    @Path(WARM_UP)
    @Consumes(APPLICATION_JSON)
    public int warmUp(
            @HeaderParam(SESSION) String session,
            @Valid WarmUpRequest request) {
        return warmUpService.warmUp(
                authService
                        .userInfoBySessionQuite(session)
                        .map(UserInfo::getUid)
                        .orElse(FILLER_LOSER_UID),
                request);
    }
}
