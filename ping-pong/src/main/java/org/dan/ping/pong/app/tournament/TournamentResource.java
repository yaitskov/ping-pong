package org.dan.ping.pong.app.tournament;

import static java.util.Arrays.asList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.dan.ping.pong.app.auth.AuthService.SESSION;
import static org.dan.ping.pong.app.bid.BidState.Here;
import static org.dan.ping.pong.app.bid.BidState.Paid;
import static org.dan.ping.pong.app.bid.BidState.Want;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;
import static org.dan.ping.pong.sys.error.PiPoEx.forbidden;

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
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;

@Slf4j
@Path("/")
@Produces(APPLICATION_JSON)
public class TournamentResource {
    private static final String TOURNAMENT = "/tournament/";
    public static final String RUNNING_TOURNAMENTS = TOURNAMENT + "running";
    public static final String TOURNAMENT_STATE = TOURNAMENT + "state";
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
    public static final String TOURNAMENT_COPY = TOURNAMENT + "copy";
    public static final String TOURNAMENT_ENLIST = TOURNAMENT + "enlist";
    public static final String TOURNAMENT_ENLIST_OFFLINE = TOURNAMENT + "enlist-offline";
    public static final String TOURNAMENT_RESIGN = TOURNAMENT + "resign";
    public static final String TOURNAMENT_EXPEL = TOURNAMENT + "expel";
    public static final String TOURNAMENT_UPDATE = TOURNAMENT + "update";
    public static final String TOURNAMENT_ENLISTED = TOURNAMENT + "enlisted";
    public static final String MY_RECENT_TOURNAMENT = "/tournament/my-recent";
    public static final String MY_RECENT_TOURNAMENT_JUDGEMENT =  TOURNAMENT + "my-recent-judgement";
    public static final String TOURNAMENT_RESULT = "/tournament/result/";
    public static final String RESULT_CATEGORY = "/category/";

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

    @POST
    @Path(TOURNAMENT_COPY)
    @Consumes(APPLICATION_JSON)
    public int copy(
            @HeaderParam(SESSION) String session,
            CopyTournament copyTournament) {
        final int uid = authService.userInfoBySession(session).getUid();
        if (tournamentDao.isAdminOf(uid, copyTournament.getOriginTid())) {
            return tournamentService.copy(copyTournament);
        } else {
            throw forbidden("You are not administrator of tournament "
                    + copyTournament.getOriginTid());
        }
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
            @Suspended AsyncResponse response,
            @HeaderParam(SESSION) String session,
            EnlistTournament enlistment) {
        if (enlistment.getCategoryId() < 1) {
            throw badRequest("Category is not set");
        }
        final UserInfo user = authService.userInfoBySession(session);
        tournamentAccessor.update(new Tid(enlistment.getTid()), response, (tournament, batch) -> {
            tournamentService.enlistOnline(enlistment, tournament, user, batch);
        });

    }

    @POST
    @Path(TOURNAMENT_ENLIST_OFFLINE)
    @Consumes(APPLICATION_JSON)
    public void enlistOffline(
            @Suspended AsyncResponse response,
            @HeaderParam(SESSION) String session,
            EnlistOffline enlistment) {
        if (enlistment.getCid() < 1) {
            throw badRequest("Category id is missing");
        }
        if (!asList(Want, Paid, Here).contains(enlistment.getBidState())) {
            throw badRequest("Bid state could be " + asList(Want, Paid, Here));
        }
        final int adminUid = authService.userInfoBySession(session).getUid();
        tournamentAccessor.update(new Tid(enlistment.getTid()), response, (tournament, batch) -> {
            tournament.checkAdmin(adminUid);
            return tournamentService.enlistOffline(tournament, enlistment, batch);
        });
    }

    @POST
    @Path(TOURNAMENT_UPDATE)
    @Consumes(APPLICATION_JSON)
    public void update(
            @Suspended AsyncResponse response,
            @HeaderParam(SESSION) String session,
            TournamentUpdate update) {
        final int adminUid = authService.userInfoBySession(session).getUid();
        tournamentAccessor.update(new Tid(update.getTid()), response, (tournament, batch) -> {
            tournament.checkAdmin(adminUid);
            tournamentService.update(tournament, update, batch);
        });

    }

    @POST
    @Path(TOURNAMENT_RESIGN)
    @Consumes(APPLICATION_JSON)
    public void resign(
            @HeaderParam(SESSION) String session,
            @Suspended AsyncResponse response,
            int tid) {
        final int uid = authService.userInfoBySession(session).getUid();
        tournamentAccessor.update(new Tid(tid), response, (tournament, batch) -> {
            tournamentService.leaveTournament(tournament.getParticipant(uid),
                    tournament, BidState.Quit, batch);
        });
    }

    @Inject
    private TournamentAccessor tournamentAccessor;

    @POST
    @Path(TOURNAMENT_EXPEL)
    @Consumes(APPLICATION_JSON)
    public void expel(
            @Suspended AsyncResponse response,
            @HeaderParam(SESSION) String session,
            ExpelParticipant expelParticipant) {
        final int uid = authService.userInfoBySession(session).getUid();
        tournamentAccessor.update(new Tid(expelParticipant.getTid()), response, (tournament, batch) -> {
            tournament.checkAdmin(uid);
            tournamentService.leaveTournament(
                    tournament.getParticipant(expelParticipant.getUid()),
                    tournament, BidState.Expl, batch);
        });
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
            @Suspended AsyncResponse response,
            @HeaderParam(SESSION) String session,
            TournamentParameters parameters) {
        int uid = authService.userInfoBySession(session).getUid();
        tournamentAccessor.update(new Tid(parameters.getTid()), response, (tournament, batch) -> {
            tournament.checkAdmin(uid);
            tournamentService.updateTournamentParams(tournament, parameters, batch);
        });
    }

    @POST
    @Path(BEGIN_TOURNAMENT)
    @Consumes(APPLICATION_JSON)
    public void beginTournament(
            @Suspended AsyncResponse response,
            @HeaderParam(SESSION) String session,
            int tid) {
        final int uid = authService.userInfoBySession(session).getUid();
        log.info("Uid {} begins tid {}", uid, tid);
        tournamentAccessor.update(new Tid(tid), response, (tournament, batch) -> {
            tournament.checkAdmin(uid);
            tournamentService.begin(tournament, batch);
        });
    }

    @POST
    @Path(CANCEL_TOURNAMENT)
    @Consumes(APPLICATION_JSON)
    public void cancelTournament(
            @Suspended AsyncResponse response,
            @HeaderParam(SESSION) String session,
            int tid) {
        final int uid = authService.userInfoBySession(session).getUid();
        tournamentAccessor.update(new Tid(tid), response, (tournament, batch) -> {
            tournament.checkAdmin(uid);
            tournamentService.cancel(tournament, batch);
        });
    }

    @POST
    @Path(TOURNAMENT_STATE)
    @Consumes(APPLICATION_JSON)
    public void setTournamentStatus(
            @Suspended AsyncResponse response,
            @HeaderParam(SESSION) String session,
            SetTournamentState stateUpdate) {
        final int uid = authService.userInfoBySession(session).getUid();
        log.info("Uid {} sets status {} for tid {}", uid,
                stateUpdate.getState(), stateUpdate.getTid());
        tournamentAccessor.update(new Tid(stateUpdate.getTid()), response, (tournament, batch) -> {
            tournament.setState(stateUpdate.getState());
            tournamentService.setTournamentState(tournament, batch);
        });
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
    @Path(TOURNAMENT_RESULT + "{tid}" + RESULT_CATEGORY + "{cid}")
    public void tournamentResult(
            @Suspended AsyncResponse response,
            @PathParam("tid") int tid,
            @PathParam("cid") int cid) {
        tournamentAccessor.read(new Tid(tid), response,
                (tournament) -> tournamentService.tournamentResult(tournament, cid));
    }

    @GET
    @Path("/tournament/complete/{tid}")
    public TournamentComplete completeInfo(@PathParam("tid") int tid) {
        return tournamentService.completeInfo(tid);
    }
}
