package org.dan.ping.pong.app.group;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.dan.ping.pong.app.tournament.TournamentService.TID;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.TournamentAccessor;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;

@Slf4j
@Path("/")
@Produces(APPLICATION_JSON)
public class GroupResource {
    public static final String GROUP_POPULATIONS = "/group/populations/";
    public static final String CID = "/cid/";

    @Inject
    private GroupService groupService;

    @Inject
    private TournamentAccessor tournamentAccessor;

    @GET
    @Path(GROUP_POPULATIONS + "{tid}" + CID + "{cid}")
    @Consumes(APPLICATION_JSON)
    public void enlist(
            @Suspended AsyncResponse response,
            @PathParam(TID) Tid tid,
            @PathParam("cid") int cid) {
        tournamentAccessor.read(tid, response,
                tournament -> groupService.populations(tournament, cid));
    }
}
