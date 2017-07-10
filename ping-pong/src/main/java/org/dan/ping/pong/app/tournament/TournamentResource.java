package org.dan.ping.pong.app.tournament;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.dan.ping.pong.app.auth.AuthService.SESSION;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.auth.AuthService;
import org.dan.ping.pong.app.user.UserInfo;

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
public class TournamentResource {
    private static final String TOURNAMENT = "/tournament/";
    public static final String RUNNING_TOURNAMENTS = TOURNAMENT + "running";
    private static final String TOURNAMENT_STATE = TOURNAMENT + "state";
    public static final String BEGIN_TOURNAMENT = TOURNAMENT + "begin";
    public static final String DRAFTING = TOURNAMENT + "drafting/";
    private static final String DRAFTING_PARAM = "drafting";
    public static final String DRAFTING_INFO = DRAFTING + "{" + DRAFTING_PARAM + "}";
    public static final String MY_TOURNAMENT = TOURNAMENT + "mine/{tid}";
    public static final String EDITABLE_TOURNAMENTS = TOURNAMENT + "editable/by/me";
    public static final String TOURNAMENT_CREATE = TOURNAMENT + "create";
    public static final String TOURNAMENT_ENLIST = TOURNAMENT + "enlist";
    public static final String TOURNAMENT_RESIGN = TOURNAMENT + "resign";
    public static final String TOURNAMENT_ENLISTED = TOURNAMENT + "enlisted";

    @Inject
    private TournamentService tournamentService;
    @Inject
    private TournamentDao tournamentDao;
    @Inject
    private AuthService authService;

    @POST
    @Path(TOURNAMENT_CREATE)
    @Consumes(APPLICATION_JSON)
    public int create(
            @HeaderParam(SESSION) String session,
            CreateTournament newTournament) {
        return tournamentService.create(
                authService.userInfoBySession(session).getUid(),
                newTournament);
    }

    @GET
    @Path(EDITABLE_TOURNAMENTS)
    public List<DatedTournamentDigest> get(
            @HeaderParam(SESSION) String session) {
        return tournamentDao.findWritableForAdmin(
                authService.userInfoBySession(session).getUid());
    }

    @POST
    @Path(TOURNAMENT_ENLIST)
    @Consumes(APPLICATION_JSON)
    public void enlist(
            @HeaderParam(SESSION) String session,
            EnlistTournament enlistment) {
        tournamentService.enlist(
                authService.userInfoBySession(session).getUid(),
                enlistment);
    }

    @POST
    @Path(TOURNAMENT_RESIGN)
    @Consumes(APPLICATION_JSON)
    public void resign(
            @HeaderParam(SESSION) String session,
            Integer tid) {
        tournamentService.resign(
                authService.userInfoBySession(session).getUid(),
                tid);
    }

    @GET
    @Path(TOURNAMENT_ENLISTED)
    @Consumes(APPLICATION_JSON)
    public List<DatedTournamentDigest> findTournamentsAmGoingTo(
            @HeaderParam(SESSION) String session) {
        return tournamentService.findInWithEnlisted(
                authService.userInfoBySession(session).getUid());
    }

    @GET
    @Path(DRAFTING)
    @Consumes(APPLICATION_JSON)
    public List<DatedTournamentDigest> findDrafting() {
        return tournamentService.findDrafting();
    }

    @GET
    @Path(RUNNING_TOURNAMENTS)
    @Consumes(APPLICATION_JSON)
    public List<OpenTournamentDigest> findRunning() {
        return tournamentService.findRunning();
    }

    @GET
    @Path(DRAFTING_INFO)
    @Consumes(APPLICATION_JSON)
    public DraftingTournamentInfo getDraftingTournament(
            @HeaderParam(SESSION) String session,
            @PathParam(DRAFTING_PARAM) int tid) {
        return tournamentService.getDraftingTournament(tid,
                authService.userInfoBySessionQuite(session)
                        .map(UserInfo::getUid));
    }

    @GET
    @Path(MY_TOURNAMENT)
    @Consumes(APPLICATION_JSON)
    public MyTournamentInfo getMyTournamentInfo(@PathParam("tid") int tid) {
        return tournamentService.getMyTournamentInfo(tid);
    }

    @POST
    @Path(BEGIN_TOURNAMENT)
    @Consumes(APPLICATION_JSON)
    public void beginTournament(@HeaderParam(SESSION) String session, int tid) {
        int uid = authService.userInfoBySession(session).getUid();
        log.info("Uid {} begins tid {}", uid, tid);
        tournamentService.begin(tid);
    }

    @POST
    @Path(TOURNAMENT_STATE)
    @Consumes(APPLICATION_JSON)
    public void setTournamentStatus(@HeaderParam(SESSION) String session,
            SetTournamentState stateUpdate) {
        final int uid = authService.userInfoBySession(session).getUid();
        log.info("Uid {} sets status {} for tid {}", uid,
                stateUpdate.getState(), stateUpdate.getTid());
        tournamentService.setTournamentStatus(uid, stateUpdate);
    }
}
