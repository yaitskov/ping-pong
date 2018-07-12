package org.dan.ping.pong.app.group;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.dan.ping.pong.app.match.MatchResource.TID_JP;
import static org.dan.ping.pong.app.tournament.TournamentService.TID;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.category.Cid;
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
    public static final String MEMBERS = "/group/members/";
    public static final String CID = "/cid/";
    public static final String GID_JP = "{gid}";
    public static final String GID = "gid";
    public static final String GROUP_RESULT = "/group/result/";
    public static final String GROUP_LIST = "/group/list/";

    @Inject
    private GroupService groupService;

    @Inject
    private TournamentAccessor tournamentAccessor;

    @GET
    @Path(GROUP_POPULATIONS + TID_JP + CID + "{cid}")
    @Consumes(APPLICATION_JSON)
    public void enlist(
            @Suspended AsyncResponse response,
            @PathParam(TID) Tid tid,
            @PathParam("cid") Cid cid) {
        tournamentAccessor.read(tid, response,
                tournament -> groupService.populations(tournament, cid));
    }

    @GET
    @Path(MEMBERS + TID_JP + "/" + GID_JP)
    @Consumes(APPLICATION_JSON)
    public void getInfoAndMembers(
            @Suspended AsyncResponse response,
            @PathParam(TID) Tid tid,
            @PathParam(GID) Gid gid) {
        tournamentAccessor.read(tid, response,
                tournament -> groupService.members(tournament, gid));
    }

    @GET
    @Path(GROUP_LIST + TID_JP)
    public void list(
            @Suspended AsyncResponse response,
            @PathParam(TID) Tid tid) {
        tournamentAccessor.read(tid, response,
                tournament -> TournamentGroups.builder()
                        .categories(tournament.getCategories().values())
                        .groups(tournament.getGroups().values())
                        .build());
    }

    @GET
    @Path(GROUP_RESULT + TID_JP + "/" + GID_JP)
    public void result(
            @Suspended AsyncResponse response,
            @PathParam(TID) Tid tid,
            @PathParam(GID) Gid gid) {
        tournamentAccessor.read(tid, response,
                tournament -> groupService.result(tournament, gid));
    }
}
