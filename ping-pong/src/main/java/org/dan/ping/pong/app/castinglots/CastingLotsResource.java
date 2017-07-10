package org.dan.ping.pong.app.castinglots;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.dan.ping.pong.app.auth.AuthService.SESSION;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.auth.AuthService;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Slf4j
@Path("/")
public class CastingLotsResource {
    public static final String CASTING_LOTS = "/casting-lots";

    @Inject
    private CastingLotsService castingLotsService;
    @Inject
    private AuthService authService;

    @POST
    @Path(CASTING_LOTS)
    @Consumes(APPLICATION_JSON)
    public void makeGroups(@HeaderParam(SESSION) String session,
            DoCastingLots doCastingLots) {
        final int uid = authService.userInfoBySession(session).getUid();
        log.info("User {} does casting lots in tournament {}",
                uid, doCastingLots.getTid());
        castingLotsService.makeGroups(doCastingLots.getTid());
    }
}
