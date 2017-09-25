package org.dan.ping.pong.app.castinglots;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.dan.ping.pong.app.auth.AuthService.SESSION;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.auth.AuthService;
import org.dan.ping.pong.app.tournament.DbUpdaterFactory;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.TournamentCache;
import org.dan.ping.pong.sys.seqex.SequentialExecutor;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;

@Slf4j
@Path("/")
public class CastingLotsResource {
    public static final String CASTING_LOTS = "/casting-lots";
    public static final String GET_MANUAL_BIDS_ORDER = CASTING_LOTS + "/manual-bids-order/tid/";
    public static final String ORDER_BIDS_MANUALLY = CASTING_LOTS + "/order-bids-manually";
    public static final String CID_IN = "/cid/";

    @Inject
    private CastingLotsService castingLotsService;
    @Inject
    private AuthService authService;
    @Inject
    private SequentialExecutor sequentialExecutor;
    @Inject
    private TournamentCache tournamentCache;
    @Inject
    private DbUpdaterFactory dbUpdaterFactory;

    @POST
    @Path(CASTING_LOTS)
    @Consumes(APPLICATION_JSON)
    public void makeGroups(
            @Suspended AsyncResponse response,
            @HeaderParam(SESSION) String session,
            DoCastingLots doCastingLots) {
        final int uid = authService.userInfoBySession(session).getUid();
        log.info("User {} does casting lots in tournament {}",
                uid, doCastingLots.getTid());
        sequentialExecutor.execute(new Tid(doCastingLots.getTid()), () -> {
            try {
                castingLotsService.seed(tournamentCache.load(doCastingLots.getTid()));
                response.resume("");
            } catch (Exception e) {
                tournamentCache.invalidate(doCastingLots.getTid());
                response.resume(e);
            }
        });
    }

    @POST
    @Path(ORDER_BIDS_MANUALLY)
    @Consumes(APPLICATION_JSON)
    public void orderBidsManually(
            @HeaderParam(SESSION) String session,
            OrderCategoryBidsManually order) {
        final int uid = authService.userInfoBySession(session).getUid();
        tournamentCache.load(order.getTid()).checkAdmin(uid);
        castingLotsService.orderCategoryBidsManually(order);
    }

    @GET
    @Path(GET_MANUAL_BIDS_ORDER + "{tid}" + CID_IN + "{cid}")
    @Consumes(APPLICATION_JSON)
    public List<RankedBid> getManualBidsOrder(
            @PathParam("tid") int tid,
            @PathParam("cid") int cid) {
        return castingLotsService.loadManualBidsOrder(tid, cid);
    }
}
