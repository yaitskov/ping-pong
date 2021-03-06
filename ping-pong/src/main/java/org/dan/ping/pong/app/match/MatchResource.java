package org.dan.ping.pong.app.match;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.dan.ping.pong.app.auth.AuthService.SESSION;
import static org.dan.ping.pong.app.bid.BidResource.TID_SLASH_UID;
import static org.dan.ping.pong.app.tournament.ParticipantMemState.BID;
import static org.dan.ping.pong.app.tournament.TournamentService.TID;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.auth.AuthService;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.TournamentAccessor;
import org.dan.ping.pong.app.user.UserInfo;
import org.dan.ping.pong.util.time.Clocker;

import java.util.Optional;

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
    public static final String MATCH_RESULT = "/match/result/";
    public static final String MATCH_LIST_PLAYED_ME = "/match/list/played-by-me/";
    public static final String MATCH_LIST_JUDGED = "/match/list/judged/";
    public static final String MY_PENDING_MATCHES = "/match/list/my/pending/";
    public static final String BID_PENDING_MATCHES = "/match/list/bid/pending/";
    public static final String OPEN_MATCHES_FOR_JUDGE = "/match/judge/list/open/";
    public static final String SCORE_SET = "/match/participant/score";
    public static final String MATCH_WATCH_LIST_OPEN = "/match/watch/list/open/";
    public static final String MATCH_RESET_SET_SCORE = "/match/reset-set-score";
    public static final String TID_JP = "{tid}";
    public static final String BID_JP = "{bid}";
    public static final String UID = "uid";
    private static final String MATCH_RULES = "/match/rules/";
    private static final String MID = "mid";
    private static final String MID_JP = "{mid}";
    public static final String MATCH_FOR_JUDGE = "/match/for-judge/";
    public static final String RESCORE_MATCH = "/match/rescore-match";
    public static final String MATCH_FIND_BY_PARTICIPANTS = "/match/find-by-participants/";

    @Inject
    private AuthService authService;

    @Inject
    private MatchDao matchDao;

    @Inject
    private MatchService matchService;

    @GET
    @Path(MATCH_FIND_BY_PARTICIPANTS + TID_JP + "/" + BID_JP + "/{bid2}")
    @Consumes(APPLICATION_JSON)
    public void findMatchesByParticipants(
            @Suspended AsyncResponse response,
            @PathParam(TID) Tid tid,
            @PathParam(BID) Bid bid1,
            @PathParam("bid2") Bid bid2) {
        tournamentAccessor.read(tid, response,
                tournament -> matchService.findMatchesByParticipants(tournament, bid1, bid2));
    }

    @GET
    @Path(MY_PENDING_MATCHES + TID_JP)
    @Consumes(APPLICATION_JSON)
    public void findPendingMatches(
            @HeaderParam(SESSION) String session,
            @Suspended AsyncResponse response,
            @PathParam(TID) Tid tid) {
        final Uid uid = authService.userInfoBySession(session).getUid();
        tournamentAccessor.read(tid, response,
                tournament -> matchService.findAllBidsOfUid(tournament, uid)
                        .stream()
                        .map((bid) -> matchService.findPendingMatchesIncludeConsole(tournament, bid))
                        .reduce(MyPendingMatchList::merge));
    }

    @GET
    @Path(BID_PENDING_MATCHES + TID_JP + TID_SLASH_UID + BID_JP)
    @Consumes(APPLICATION_JSON)
    public void findBidPendingMatches(
            @Suspended AsyncResponse response,
            @PathParam(TID) Tid tid,
            @PathParam(BID) Bid bid) {
        tournamentAccessor.read(tid, response,
                tournament -> matchService.findPendingMatchesIncludeConsole(tournament, bid));
    }

    @GET
    @Path(MATCH_RESULT + TID_JP + TID_SLASH_UID + MID_JP)
    @Consumes(APPLICATION_JSON)
    public void result(
            @Suspended AsyncResponse response,
            @HeaderParam(SESSION) String session,
            @PathParam(TID) Tid tid,
            @PathParam(MID) Mid mid) {
        final Optional<Uid> ouid = authService.userInfoBySessionQuite(session).map(UserInfo::getUid);
        tournamentAccessor.read(tid, response,
                tournament -> matchService.matchResult(tournament, mid, ouid));
    }

    @GET
    @Path(MATCH_LIST_PLAYED_ME + TID_JP)
    @Consumes(APPLICATION_JSON)
    public void findPlayedMatchesByMe(
            @HeaderParam(SESSION) String session,
            @Suspended AsyncResponse response,
            @PathParam(TID) Tid tid) {
        final Uid uid = authService.userInfoBySession(session).getUid();
        tournamentAccessor.read(tid, response,
                tournament -> matchService.findAllBidsOfUid(tournament, uid)
                        .stream()
                        .map((bid) -> matchService.findPlayedMatchesByBid(tournament, bid))
                        .reduce(PlayedMatchList::merge));
    }

    @GET
    @Path(MATCH_LIST_JUDGED + TID_JP + TID_SLASH_UID + BID_JP)
    public void findJudgedMatches(
            @Suspended AsyncResponse response,
            @PathParam(TID) Tid tid,
            @PathParam(BID) Bid bid) {
        tournamentAccessor.read(tid, response,
                tournament -> matchService.findPlayedMatchesByBid(tournament, bid));
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
            SetScoreReq score) {
        final Uid uid = authService.userInfoBySession(session).getUid();
        log.info("User {} sets scores {} for match {}",
                uid, score.getScores(), score.getMid());
        tournamentAccessor.update(score.getTid(), response,
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
        log.info("User {} reset scores in mid {} of tid {}",
                uid, reset.getMid(), reset.getTid());
        tournamentAccessor.update(reset.getTid(), response,
                (tournament, batch) -> {
                    tournament.checkAdmin(uid);
                    matchEditorService.resetMatchScore(tournament, reset, batch);
                });
    }

    @Inject
    private MatchEditorService matchEditorService;

    @POST
    @Path(RESCORE_MATCH)
    public void rescoreMatch(
            @Suspended AsyncResponse response,
            @HeaderParam(SESSION) String session,
            RescoreMatch rescoreMatch) {
        final Uid uid = authService.userInfoBySession(session).getUid();
        log.info("User {} rescores match {}", uid, rescoreMatch);
        tournamentAccessor.update(rescoreMatch.getTid(), response,
                (tournament, batch) -> {
                    tournament.checkAdmin(uid);
                    matchEditorService.rescoreMatch(tournament, rescoreMatch, batch);
                });
    }

    @GET
    @Path(MATCH_RULES + TID_JP)
    public void getMatchRules(
            @Suspended AsyncResponse response,
            @PathParam(TID) Tid tid) {
        tournamentAccessor.read(tid, response, tournament -> tournament.getRule().getMatch());
    }

    @GET
    @Path(OPEN_MATCHES_FOR_JUDGE + TID_JP)
    @Consumes(APPLICATION_JSON)
    public void findOpenMatchesForJudge(
            @PathParam(TID) Tid tid,
            @Suspended AsyncResponse response) {
        tournamentAccessor.read(tid, response, matchService::findOpenMatchesFurJudgeIncludingConsole);
    }

    @GET
    @Path(MATCH_WATCH_LIST_OPEN + TID_JP)
    public void findOpenMatchesForWatching(
            @Suspended AsyncResponse response,
            @PathParam(TID) Tid tid) {
        tournamentAccessor.read(tid, response, matchService::findOpenMatchesForWatching);
    }

    @GET
    @Path(MATCH_FOR_JUDGE + TID_JP + "/" + MID_JP)
    public void getMatchForJudge(
            @Suspended AsyncResponse response,
            @PathParam(TID) Tid tid,
            @PathParam(MID) Mid mid) {
        tournamentAccessor.read(tid, response,
                tournament -> matchService.getMatchForJudge(tournament, mid));
    }
}
