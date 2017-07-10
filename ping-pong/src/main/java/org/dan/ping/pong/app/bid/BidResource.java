package org.dan.ping.pong.app.bid;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.dan.ping.pong.app.auth.AuthService.SESSION;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.auth.AuthService;
import org.dan.ping.pong.app.user.UserLink;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

@Slf4j
@Path("/")
@Produces(APPLICATION_JSON)
public class BidResource {
    public static final String BID_PAID = "/bid/paid";
    public static final String BID_READY_TO_PLAY = "/bid/ready-to-play";
    @Inject
    private AuthService authService;

    @Inject
    private BidService bidService;

    @POST
    @Path(BID_PAID)
    @Consumes(APPLICATION_JSON)
    public void paid(@HeaderParam(SESSION) String session, BidId bidId) {
        log.info("Admin {} took money for bid {}",
                authService.userInfoBySession(session).getUid(),
                bidId);
        bidService.paid(bidId);
    }

    @POST
    @Path(BID_READY_TO_PLAY)
    @Consumes(APPLICATION_JSON)
    public void readyToPlay(@HeaderParam(SESSION) String session, BidId bidId) {
        log.info("Admin {} checked that bid {} arrived to tournament in good fit",
                authService.userInfoBySession(session).getUid(),
                bidId);
        bidService.readyToPlay(bidId);
    }

    @POST
    @Path("/bid/disappeared")
    @Consumes(APPLICATION_JSON)
    public void disappeared(@HeaderParam(SESSION) String session, BidId bidId) {
        log.info("Admin {} checked that bid {} disappeared",
                authService.userInfoBySession(session).getUid(),
                bidId);
        bidService.disappeared(bidId);
    }

    @GET
    @Path("/bid/enlisted-to-be-checked/{tid}")
    public List<ParticipantState> enlistedToBeChecked(
            @HeaderParam(SESSION) String session,
            @PathParam("tid") int tid) {
        return bidService.findEnlisted(tid);
    }
}
