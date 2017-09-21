package org.dan.ping.pong.app.castinglots;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.dan.ping.pong.app.auth.AuthService.SESSION;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.auth.AuthService;
import org.dan.ping.pong.app.tournament.DbUpdaterFactory;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.TournamentCache;
import org.dan.ping.pong.sys.seqex.SequentialExecutor;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;

@Slf4j
@Path("/")
public class CastingLotsResource {
    public static final String CASTING_LOTS = "/casting-lots";

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
}
