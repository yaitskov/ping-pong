package org.dan.ping.pong.app.tournament;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.dan.ping.pong.app.auth.AuthService.SESSION;
import static org.dan.ping.pong.app.category.CategoryResource.CID;
import static org.dan.ping.pong.app.category.CategoryResource.CID_JP;
import static org.dan.ping.pong.app.match.MatchResource.TID_JP;
import static org.dan.ping.pong.app.tournament.TournamentCache.TOURNAMENT_CACHE;
import static org.dan.ping.pong.app.tournament.TournamentService.TID;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;
import static org.dan.ping.pong.sys.error.PiPoEx.forbidden;

import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.auth.AuthService;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.category.Cid;
import org.dan.ping.pong.app.tournament.rules.TournamentRulesValidator;
import org.dan.ping.pong.app.user.UserInfo;
import org.dan.ping.pong.sys.validation.TidBodyRequired;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.Valid;
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
    public static final String DRAFTING = "tournament/drafting/";
    public static final String MY_TOURNAMENT = "/tournament/mine/";
    public static final String TOURNAMENT_RULES = TOURNAMENT + "rules";
    public static final String GET_TOURNAMENT_RULES = TOURNAMENT_RULES + "/";
    public static final String EDITABLE_TOURNAMENTS = TOURNAMENT + "editable/by/me";
    public static final String TOURNAMENT_CREATE = TOURNAMENT + "create";
    public static final String TOURNAMENT_INVALIDATE_CACHE = "/tournament/invalidate/cache";
    public static final String TOURNAMENT_COPY = TOURNAMENT + "copy";
    public static final String TOURNAMENT_ENLIST = TOURNAMENT + "enlist";
    public static final String TOURNAMENT_ENLIST_OFFLINE = TOURNAMENT + "enlist-offline";
    public static final String TOURNAMENT_RESIGN = "/tournament/resign";
    public static final String TOURNAMENT_EXPEL = TOURNAMENT + "expel";
    public static final String TOURNAMENT_UPDATE = TOURNAMENT + "update";
    public static final String TOURNAMENT_ENLISTED = "/tournament/enlisted";
    public static final String MY_RECENT_TOURNAMENT = "/tournament/my-recent";
    public static final String MY_RECENT_TOURNAMENT_JUDGEMENT =  TOURNAMENT + "my-recent-judgement";
    public static final String TOURNAMENT_RESULT = "/tournament/result/";
    public static final String RESULT_CATEGORY = "/category/";
    public static final String TOURNAMENT_COMPLETE = "/tournament/complete/";
    public static final String TOURNAMENT_PLAY_OFF_MATCHES = "/tournament/play-off-matches/";
    public static final String TOURNAMENT_CONSOLE_CREATE = "/tournament/console/create";
    public static final String TOURNAMENT_FOLLOWING = "/tournament/following/";

    @Inject
    private TournamentService tournamentService;
    @Inject
    private TournamentDaoMySql tournamentDao;
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
        newTournament.validateNew();
        rulesValidator.validate(newTournament.getRules());
        return tournamentService.create(
                authService.userInfoBySession(session).getUid(),
                newTournament);
    }

    @POST
    @Path(TOURNAMENT_CONSOLE_CREATE)
    @Consumes(APPLICATION_JSON)
    public void createConsole(
            @HeaderParam(SESSION) String session,
            @Suspended AsyncResponse response,
            @TidBodyRequired @Valid Tid parentId) {
        final UserInfo user = authService.userInfoBySession(session);
        tournamentAccessor.update(parentId, response, (tournament, batch) -> {
            tournament.checkAdmin(user.getUid());
            return tournamentService.createConsoleFor(tournament, user, batch);
        });
    }

    @POST
    @Path(TOURNAMENT_COPY)
    @Consumes(APPLICATION_JSON)
    public Tid copy(
            @HeaderParam(SESSION) String session,
            CopyTournament copyTournament) {
        final Uid uid = authService.userInfoBySession(session).getUid();
        log.info("User {} copies tournament {}", uid, copyTournament.getOriginTid());
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
            @Valid EnlistTournament enlistment) {
        if (enlistment.getBidState() != BidState.Want) {
            throw badRequest("Bid state must be " + BidState.Want);
        }
        final UserInfo user = authService.userInfoBySession(session);
        tournamentAccessor.update(
                enlistment.getTid(),
                response,
                (tournament, batch) ->  {
                    return tournamentService.enlistOnline(enlistment, tournament, user, batch);
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
            @TidBodyRequired @Valid Tid tid) {
        final Uid adminUid = authService.userInfoBySession(session).getUid();
        tournamentAccessor.update(tid, response, (tournament, batch) -> {
            tournament.checkAdmin(adminUid);
            log.info("invalidate tournament cache {}", tid);
            tournamentCache.invalidate(tid);
        });
    }

    @POST
    @Path(TOURNAMENT_ENLIST_OFFLINE)
    @Consumes(APPLICATION_JSON)
    public void enlistOffline(
            @Suspended AsyncResponse response,
            @HeaderParam(SESSION) String session,
            @Valid EnlistOffline enlistment) {
        final Uid adminUid = authService.userInfoBySession(session).getUid();
        tournamentAccessor.update(enlistment.getTid(), response, (tournament, batch) -> {
            tournament.checkAdmin(adminUid);
            return tournamentService.enlistOffline(adminUid, tournament, enlistment, batch);
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
            ResignTournament resign) {
        final Uid uid = authService.userInfoBySession(session).getUid();
        tournamentAccessor.update(resign.getTid(), response, (tournament, batch) -> {
            tournament.findBidsByUid(uid).stream()
                    .map(tournament::getParticipant)
                    .filter(p -> resign.getCid()
                            .map(cid -> cid.equals(p.getCid())).orElse(true))
                    .forEach(bid -> tournamentService.leaveTournament(
                            bid, tournament, BidState.Quit, batch));
        });
    }

    @Inject
    private TournamentAccessor tournamentAccessor;

    private static final Set<BidState> acceptableBidExpelTargetStates
            = ImmutableSet.of(BidState.Expl, BidState.Quit);

    @POST
    @Path(TOURNAMENT_EXPEL)
    @Consumes(APPLICATION_JSON)
    public void expel(
            @Suspended AsyncResponse response,
            @HeaderParam(SESSION) String session,
            @Valid ExpelParticipant expelParticipant) {
        if (!acceptableBidExpelTargetStates.contains(expelParticipant.getTargetBidState())) {
            throw badRequest("bid target state is out of range");
        }
        final Uid uid = authService.userInfoBySession(session).getUid();
        tournamentAccessor.update(expelParticipant.getTid(), response, (tournament, batch) -> {
            tournament.checkAdmin(uid);
            tournamentService.leaveTournament(
                    tournament.getParticipant(expelParticipant.getBid()),
                    tournament, expelParticipant.getTargetBidState(), batch);
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
    public void updateTournamentRules(
            @Suspended AsyncResponse response,
            @HeaderParam(SESSION) String session,
            TidIdentifiedRules parameters) {
        rulesValidator.validate(parameters.getRules());
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
            tournamentService.beginAndSchedule(tournament, batch);
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

    @Inject
    private TournamentTerminator tournamentTerminator;

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
            tournamentTerminator.setTournamentState(tournament, stateUpdate.getState(), batch);
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
    @Path(TOURNAMENT_RESULT + TID_JP + RESULT_CATEGORY + CID_JP)
    public void tournamentResult(
            @Suspended AsyncResponse response,
            @PathParam(TID) Tid tid,
            @PathParam(CID) Cid cid) {
        tournamentAccessor.read(tid, response,
                (tournament) -> tournamentService.tournamentResult(tournament, cid));
    }

    @GET
    @Path(TOURNAMENT_COMPLETE + TID_JP)
    public void completeInfo(
            @Suspended AsyncResponse response,
            @PathParam(TID) Tid tid) {
        tournamentAccessor.read(tid, response,
                (tournament) -> tournamentService.completeInfo(tournament));
    }

    @GET
    @Path(TOURNAMENT_PLAY_OFF_MATCHES + TID_JP + "/" + CID_JP)
    public void playOffMatches(
            @Suspended AsyncResponse response,
            @PathParam(TID) Tid tid,
            @PathParam(CID) Cid cid) {
        tournamentAccessor.read(tid, response,
                (tournament) -> tournamentService.playOffMatches(tournament, cid));
    }

    @GET
    @Path(TOURNAMENT_FOLLOWING + TID_JP)
    public List<TournamentDigest> following(@Valid @PathParam(TID) Tid tid) {
        return tournamentService.findFollowingFrom(tid);
    }
}
