package org.dan.ping.pong.app.tournament.marshaling;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.dan.ping.pong.app.auth.AuthService.SESSION;
import static org.dan.ping.pong.app.match.MatchResource.TID_JP;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.TournamentAccessor;
import org.dan.ping.pong.sys.validation.TidBodyRequired;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;

@Slf4j
@Path("/")
@Produces(APPLICATION_JSON)
public class TournamentMarshalingResource {
    public static final String TOURNAMENT_EXPORT_STATE = "/tournament/export/state/";

    @Inject
    private TournamentMarshalingService marshalingService;

    @Inject
    private TournamentAccessor tournamentAccessor;

    @GET
    @Path(TOURNAMENT_EXPORT_STATE + TID_JP)
    public void exportState(
            @Suspended AsyncResponse response,
            @HeaderParam(SESSION) String session,
            @TidBodyRequired @Valid Tid tid) {
        tournamentAccessor.read(tid, response, marshalingService::exportState);
    }
}
