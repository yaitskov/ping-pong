package org.dan.ping.pong.app.castinglots;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.dan.ping.pong.app.auth.AuthService.SESSION;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.auth.AuthService;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.category.Cid;
import org.dan.ping.pong.app.tournament.DbUpdaterFactory;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.TournamentAccessor;
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

    @Inject
    private TournamentAccessor tournamentAccessor;

    @POST
    @Path(ORDER_BIDS_MANUALLY)
    @Consumes(APPLICATION_JSON)
    public void orderBidsManually(
            @HeaderParam(SESSION) String session,
            OrderCategoryBidsManually order) {
        final Uid uid = authService.userInfoBySession(session).getUid();
        tournamentCache.load(order.getTid()).checkAdmin(uid);
        castingLotsService.orderCategoryBidsManually(order);
    }

    @GET
    @Path(GET_MANUAL_BIDS_ORDER + "{tid}" + CID_IN + "{cid}")
    @Consumes(APPLICATION_JSON)
    public List<RankedBid> getManualBidsOrder(
            @PathParam("tid") Tid tid,
            @PathParam("cid") Cid cid) {
        return castingLotsService.loadManualBidsOrder(tid, cid);
    }
}
