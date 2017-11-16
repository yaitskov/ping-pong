package org.dan.ping.pong.app.tournament;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.dan.ping.pong.app.auth.AuthService.SESSION;
import static org.dan.ping.pong.app.match.MatchResource.TID_JP;
import static org.dan.ping.pong.app.tournament.TournamentCacheFactory.TOURNAMENT_CACHE;
import static org.dan.ping.pong.app.tournament.TournamentService.TID;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;
import static org.dan.ping.pong.sys.error.PiPoEx.forbidden;

import com.google.common.cache.LoadingCache;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.auth.AuthService;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.tournament.rules.TournamentRulesValidator;
import org.dan.ping.pong.app.tournament.rules.ValidationError;
import org.dan.ping.pong.app.user.UserInfo;
import org.dan.ping.pong.sys.error.ValidationErrors;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
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
    public static final String MY_TOURNAMENT = TOURNAMENT + "mine/";
    public static final String TOURNAMENT_RULES = TOURNAMENT + "rules";
    public static final String GET_TOURNAMENT_RULES = TOURNAMENT_RULES + "/";
    public static final String EDITABLE_TOURNAMENTS = TOURNAMENT + "editable/by/me";
    public static final String TOURNAMENT_CREATE = TOURNAMENT + "create";
    public static final String TOURNAMENT_INVALIDATE_CACHE = TOURNAMENT + "invalidate/cache";
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
    @Inject
    private TournamentRulesValidator rulesValidator;

    @POST
    @Path(TOURNAMENT_CREATE)
    @Consumes(APPLICATION_JSON)
    public Tid create(
            @HeaderParam(SESSION) String session,
            CreateTournament newTournament) {
        if (newTournament.getRules() == null) {
            throw badRequest("No rules");
        }
        validate(newTournament.getRules());
        return tournamentService.create(
                authService.userInfoBySession(session).getUid(),
                newTournament);
    }

    private void validate(TournamentRules rules) {
        final Multimap<String, ValidationError> errors = HashMultimap.create();
        rulesValidator.validate(rules, errors);
        if (!errors.isEmpty()) {
            throw badRequest(new ValidationErrors("tournament-rules-are-wrong", errors));
        }
    }

    @POST
    @Path(TOURNAMENT_COPY)
    @Consumes(APPLICATION_JSON)
    public Tid copy(
            @HeaderParam(SESSION) String session,
            CopyTournament copyTournament) {
        final Uid uid = authService.userInfoBySession(session).getUid();
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
        tournamentAccessor.update(enlistment.getTid(), response, (tournament, batch) -> {
            tournamentService.enlistOnline(enlistment, tournament, user, batch);
        });
    }

    @Inject
    @Named(TOURNAMENT_CACHE)
    private LoadingCache<Tid, TournamentMemState> tournamentCache;

    @POST
    @Path(TOURNAMENT_INVALIDATE_CACHE)
    @Consumes(APPLICATION_JSON)
    public void invalidateCache(
            @Suspended AsyncResponse response,
            @HeaderParam(SESSION) String session,
            int tid) {

        final Uid adminUid = authService.userInfoBySession(session).getUid();
        tournamentAccessor.update(new Tid(tid), response, (tournament, batch) -> {
            tournament.checkAdmin(adminUid);
            log.info("invalidate tournament cache {}", tid);
            tournamentCache.invalidate(new Tid(tid));
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
        final Uid adminUid = authService.userInfoBySession(session).getUid();
        tournamentAccessor.update(enlistment.getTid(), response, (tournament, batch) -> {
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
        final Uid adminUid = authService.userInfoBySession(session).getUid();
        tournamentAccessor.update(update.getTid(), response, (tournament, batch) -> {
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
        final Uid uid = authService.userInfoBySession(session).getUid();
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
        final Uid uid = authService.userInfoBySession(session).getUid();
        tournamentAccessor.update(expelParticipant.getTid(), response, (tournament, batch) -> {
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
    @Path(DRAFTING + TID_JP)
    @Consumes(APPLICATION_JSON)
    public void getDraftingTournament(
            @Suspended AsyncResponse response,
            @HeaderParam(SESSION) String session,
            @PathParam(TID) Tid tid) {
        tournamentAccessor.read(tid, response,
                tournament -> tournamentService.getDraftingTournament(tournament,
                        authService.userInfoBySessionQuite(session)
                                .map(UserInfo::getUid)));
    }

    @GET
    @Path(MY_TOURNAMENT + TID_JP)
    @Consumes(APPLICATION_JSON)
    public void getMyTournamentInfo(
            @Suspended AsyncResponse response,
            @PathParam(TID) Tid tid) {
        tournamentAccessor.read(tid, response,
                tournament -> tournamentService.getMyTournamentInfo(tournament));
    }

    @GET
    @Path(GET_TOURNAMENT_RULES + TID_JP)
    @Consumes(APPLICATION_JSON)
    public void getTournamentRules(
            @Suspended AsyncResponse response,
            @PathParam(TID) Tid tid) {
        tournamentAccessor.read(tid, response, TournamentMemState::getRule);
    }

    @POST
    @Path(TOURNAMENT_RULES)
    @Consumes(APPLICATION_JSON)
    public void updateTournamentParams(
            @Suspended AsyncResponse response,
            @HeaderParam(SESSION) String session,
            TidIdentifiedRules parameters) {
        validate(parameters.getRules());
        Uid uid = authService.userInfoBySession(session).getUid();
        tournamentAccessor.update(parameters.getTid(), response, (tournament, batch) -> {
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
        final Uid uid = authService.userInfoBySession(session).getUid();
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
        final Uid uid = authService.userInfoBySession(session).getUid();
        log.info("User {} tried to cancel tournament {}", uid, tid);
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
        final Uid uid = authService.userInfoBySession(session).getUid();
        log.info("Uid {} sets status {} for tid {}", uid,
                stateUpdate.getState(), stateUpdate.getTid());
        tournamentAccessor.update(stateUpdate.getTid(), response, (tournament, batch) -> {
            tournamentService.setTournamentState(tournament, stateUpdate.getState(), batch);
        });
    }

    @GET
    @Path(MY_RECENT_TOURNAMENT)
    public MyRecentTournaments findMyRecentPlayedTournaments(
            @HeaderParam(SESSION) String session) {
        final Uid uid = authService.userInfoBySession(session).getUid();
        return tournamentService.findMyRecentTournaments(uid);
    }

    @GET
    @Path(MY_RECENT_TOURNAMENT_JUDGEMENT)
    public MyRecentJudgedTournaments findMyRecentJudgedTournaments(
            @HeaderParam(SESSION) String session) {
        final Uid uid = authService.userInfoBySession(session).getUid();
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
    public TournamentComplete completeInfo(@PathParam("tid") Tid tid) {
        return tournamentService.completeInfo(tid);
    }
}
