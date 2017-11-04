package org.dan.ping.pong.app.match.dispute;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.dan.ping.pong.app.auth.AuthService.SESSION;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.auth.AuthService;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.tournament.TournamentAccessor;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;

@Slf4j
@Path("/")
@Produces(APPLICATION_JSON)
public class MatchDisputeResource {
    @Inject
    private AuthService authService;

    @Inject
    private MatchDisputeService matchDisputeService;

    @Inject
    private TournamentAccessor tournamentAccessor;


    @POST
    @Path("/dispute/open")
    @Consumes(APPLICATION_JSON)
    public void open(
            @Suspended AsyncResponse response,
            @HeaderParam(SESSION) String session,
            DisputeClaimRequest claim) {
        final Uid uid = authService.userInfoBySession(session).getUid();
        log.info("User {} claims a dispute {}", uid, claim);
        tournamentAccessor.update(claim.getTid(), response,
                (tournament, batch) -> {
                    return matchDisputeService.openDispute(tournament, claim, batch, uid);
                });
    }
}
