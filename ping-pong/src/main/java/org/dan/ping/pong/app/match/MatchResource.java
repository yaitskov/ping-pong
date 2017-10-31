package org.dan.ping.pong.app.match;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.dan.ping.pong.app.auth.AuthService.SESSION;
import static org.dan.ping.pong.app.bid.BidResource.TID_SLASH_UID;
import static org.dan.ping.pong.app.tournament.TournamentService.TID;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.auth.AuthService;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.TournamentAccessor;
import org.dan.ping.pong.app.tournament.Uid;
import org.dan.ping.pong.app.user.UserLink;
import org.dan.ping.pong.util.time.Clocker;

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
public class MatchResource {
    public static final String MY_PENDING_MATCHES = "/match/list/my/pending/";
    public static final String BID_PENDING_MATCHES = "/match/list/bid/pending/";
    public static final String OPEN_MATCHES_FOR_JUDGE = "/match/judge/list/open/";
    public static final String COMPLETE_MATCHES = "/match/list/completed/";
    public static final String SCORE_SET = "/match/participant/score";
    public static final String MATCH_WATCH_LIST_OPEN = "/match/watch/list/open/";
    public static final String MATCH_RESET_SET_SCORE = "/match/reset-set-score";
    public static final String TID_JP = "{tid}";
    public static final String UID_JP = "{uid}";
    public static final String UID = "uid";

    @Inject
    private AuthService authService;

    @Inject
    private MatchDao matchDao;

    @Inject
    private MatchService matchService;

    @GET
    @Path(MY_PENDING_MATCHES + TID_JP)
    @Consumes(APPLICATION_JSON)
    public void findPendingMatches(
            @HeaderParam(SESSION) String session,
            @Suspended AsyncResponse response,
            @PathParam(TID) Tid tid) {
        final Uid uid = authService.userInfoBySession(session).getUid();
        tournamentAccessor.read(tid, response,
                tournament -> matchService.findPendingMatches(tournament, uid));
    }

    @GET
    @Path(BID_PENDING_MATCHES + TID_JP + TID_SLASH_UID + UID_JP)
    @Consumes(APPLICATION_JSON)
    public void findBidPendingMatches(
            @Suspended AsyncResponse response,
            @PathParam(TID) Tid tid,
            @PathParam(UID) Uid uid) {
        tournamentAccessor.read(tid, response,
                tournament -> matchService.findPendingMatches(tournament, uid));
    }

    @Inject
    private Clocker clocker;

    @Inject
    private TournamentAccessor tournamentAccessor;

    @POST
    @Path(SCORE_SET)
    @Consumes(APPLICATION_JSON)
    public void scoreSetAndCompleteIfWinOrLose(
            @Suspended AsyncResponse response,
            @HeaderParam(SESSION) String session,
            FinalMatchScore score) {
        //response.setTimeout(30, TimeUnit.SECONDS);
        final Uid uid = authService.userInfoBySession(session).getUid();
        log.info("User {} sets scores {} for match {}",
                uid, score.getScores(), score.getMid());
        tournamentAccessor.update(new Tid(score.getTid()), response,
                (tournament, batch) -> {
                    return matchService.scoreSet(tournament, uid, score, clocker.get(), batch);
                });
    }

    @POST
    @Path(MATCH_RESET_SET_SCORE)
    public void resetSetScore(
            @Suspended AsyncResponse response,
            @HeaderParam(SESSION) String session,
            ResetSetScore reset) {
        final Uid uid = authService.userInfoBySession(session).getUid();
        log.info("User {} reset scores in min {} of tid {}",
                uid, reset.getMid(), reset.getTid());
        tournamentAccessor.update(new Tid(reset.getTid()), response,
                (tournament, batch) -> {
                    tournament.checkAdmin(uid);
                    matchService.resetMatchScore(tournament, reset, batch);
                });
    }

    @GET
    @Path("/match/rules/{tid}")
    public void getMatchRules(
            @Suspended AsyncResponse response,
            @PathParam("tid") Tid tid) {
        tournamentAccessor.read(tid, response, tournament -> tournament.getRule().getMatch());
    }

    @GET
    @Path("/match/tournament-winners/{tid}")
    public List<UserLink> findWinners(@PathParam("tid") int tid) {
        return matchDao.findWinners(tid);
    }

    @GET
    @Path(OPEN_MATCHES_FOR_JUDGE + TID_JP)
    @Consumes(APPLICATION_JSON)
    public void findOpenMatchesForJudge(
            @PathParam(TID) Tid tid,
            @Suspended AsyncResponse response) {
        tournamentAccessor.update(tid, response,
                (tournament, batch) -> {
                    return matchService.findOpenMatchesForJudgeList(tournament);
                });
    }

    @GET
    @Path(MATCH_WATCH_LIST_OPEN + "{tid}")
    public void findOpenMatchesForWatching(
            @Suspended AsyncResponse response,
            @PathParam("tid") Tid tid) {
        tournamentAccessor.update(tid, response,
                (tournament, batch) -> {
                    return matchService.findOpenMatchesForWatching(tournament);
                });
    }

    @GET
    @Path(COMPLETE_MATCHES + "{tid}")
    @Consumes(APPLICATION_JSON)
    public List<CompleteMatch> findCompleteMatches(@PathParam("tid") Integer tid) {
        return matchDao.findCompleteMatches(tid);
    }
}
