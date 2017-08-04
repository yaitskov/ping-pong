package org.dan.ping.pong.app.tournament;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.dan.ping.pong.app.auth.AuthService.SESSION;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.auth.AuthService;
import org.dan.ping.pong.app.bid.BidState;
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
    public static final String CANCEL_TOURNAMENT = TOURNAMENT + "cancel";
    public static final String DRAFTING = TOURNAMENT + "drafting/";
    private static final String DRAFTING_PARAM = "drafting";
    public static final String DRAFTING_INFO = DRAFTING + "{" + DRAFTING_PARAM + "}";
    public static final String MY_TOURNAMENT = TOURNAMENT + "mine/{tid}";
    public static final String TOURNAMENT_PARAMS = TOURNAMENT + "params";
    public static final String GET_TOURNAMENT_PARAMS = TOURNAMENT_PARAMS + "/{tid}";
    public static final String EDITABLE_TOURNAMENTS = TOURNAMENT + "editable/by/me";
    public static final String TOURNAMENT_CREATE = TOURNAMENT + "create";
    public static final String TOURNAMENT_ENLIST = TOURNAMENT + "enlist";
    public static final String TOURNAMENT_RESIGN = TOURNAMENT + "resign";
    public static final String TOURNAMENT_UPDATE = TOURNAMENT + "update";
    public static final String TOURNAMENT_ENLISTED = TOURNAMENT + "enlisted";
    public static final String MY_RECENT_TOURNAMENT = "/tournament/my-recent";
    public static final String MY_RECENT_TOURNAMENT_JUDGEMENT =  TOURNAMENT + "my-recent-judgement";

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
        if (newTournament.getMatchScore() <= 0) {
            throw badRequest("Match Score is not positive");
        }
        return tournamentService.create(
                authService.userInfoBySession(session).getUid(),
                newTournament);
    }

    @GET
    @Path(EDITABLE_TOURNAMENTS)
    public List<TournamentDigest> findTournamentsIamJudging(
            @HeaderParam(SESSION) String session) {
        return findTournamentsIamJudging(session, 0);
    }

    @GET
    @Path(EDITABLE_TOURNAMENTS + "/{days}")
    public List<TournamentDigest> findTournamentsIamJudging(
            @HeaderParam(SESSION) String session,
            @PathParam("days") int days) {
        return tournamentService.findWritableForAdmin(
                authService.userInfoBySession(session).getUid(),
                days);
    }

    @POST
    @Path(TOURNAMENT_ENLIST)
    @Consumes(APPLICATION_JSON)
    public void enlist(
            @HeaderParam(SESSION) String session,
            EnlistTournament enlistment) {
        if (enlistment.getCategoryId() < 1) {
            throw badRequest("Category is not set");
        }
        tournamentService.enlist(
                authService.userInfoBySession(session).getUid(),
                enlistment);
    }

    @POST
    @Path(TOURNAMENT_UPDATE)
    @Consumes(APPLICATION_JSON)
    public void update(
            @HeaderParam(SESSION) String session,
            TournamentUpdate update) {
        tournamentService.update(
                authService.userInfoBySession(session).getUid(),
                update);
    }

    @POST
    @Path(TOURNAMENT_RESIGN)
    @Consumes(APPLICATION_JSON)
    public void resign(
            @HeaderParam(SESSION) String session,
            int tid) {
        final int uid = authService.userInfoBySession(session).getUid();
        tournamentService.resign(uid, tid, BidState.Quit);
    }

    @GET
    @Path(TOURNAMENT_ENLISTED)
    @Consumes(APPLICATION_JSON)
    public List<TournamentDigest> findTournamentsAmGoingTo(
            @HeaderParam(SESSION) String session) {
        return findTournamentsAmGoingTo(session, 0);
    }

    @GET
    @Path(TOURNAMENT_ENLISTED + "/{days}")
    @Consumes(APPLICATION_JSON)
    public List<TournamentDigest> findTournamentsAmGoingTo(
            @HeaderParam(SESSION) String session,
            @PathParam("days") int days) {
        return tournamentService.findInWithEnlisted(
                authService.userInfoBySession(session).getUid(), days);
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
        return findRunning(0);
    }

    @GET
    @Path(RUNNING_TOURNAMENTS + "/{days}")
    @Consumes(APPLICATION_JSON)
    public List<OpenTournamentDigest> findRunning(@PathParam("days") int days) {
        return tournamentService.findRunning(days);
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

    @GET
    @Path(GET_TOURNAMENT_PARAMS)
    @Consumes(APPLICATION_JSON)
    public TournamentParameters getTournamentParams(@PathParam("tid") int tid) {
        return tournamentService.getTournamentParams(tid);
    }

    @POST
    @Path(TOURNAMENT_PARAMS)
    @Consumes(APPLICATION_JSON)
    public void updateTournamentParams(
            @HeaderParam(SESSION) String session,
            TournamentParameters parameters) {
        int uid = authService.userInfoBySession(session).getUid();
        tournamentService.updateTournamentParams(uid, parameters);
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
    @Path(CANCEL_TOURNAMENT)
    @Consumes(APPLICATION_JSON)
    public void cancelTournament(@HeaderParam(SESSION) String session, int tid) {
        final int uid = authService.userInfoBySession(session).getUid();
        tournamentService.cancel(uid, tid);
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

    @GET
    @Path(MY_RECENT_TOURNAMENT)
    public MyRecentTournaments findMyRecentPlayedTournaments(
            @HeaderParam(SESSION) String session) {
        final int uid = authService.userInfoBySession(session).getUid();
        return tournamentService.findMyRecentTournaments(uid);
    }

    @GET
    @Path(MY_RECENT_TOURNAMENT_JUDGEMENT)
    public MyRecentJudgedTournaments findMyRecentJudgedTournaments(
            @HeaderParam(SESSION) String session) {
        final int uid = authService.userInfoBySession(session).getUid();
        return tournamentService.findMyRecentJudgedTournaments(uid);
    }

    @GET
    @Path("/tournament/result/{tid}/category/{cid}")
    public List<TournamentResultEntry> tournamentResult(
            @PathParam("tid") int tid,
            @PathParam("cid") int cid) {
        return tournamentService.tournamentResult(tid, cid);
    }

    @GET
    @Path("/tournament/complete/{tid}")
    public TournamentComplete completeInfo(@PathParam("tid") int tid) {
        return tournamentService.completeInfo(tid);
    }
}
