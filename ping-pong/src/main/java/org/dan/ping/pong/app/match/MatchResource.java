package org.dan.ping.pong.app.match;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.dan.ping.pong.app.auth.AuthService.SESSION;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.auth.AuthService;
import org.dan.ping.pong.app.tournament.MatchValidationRule;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.TournamentAccessor;
import org.dan.ping.pong.app.user.UserLink;
import org.dan.ping.pong.util.time.Clocker;

import java.util.List;
import java.util.concurrent.TimeUnit;

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
    public static final String MY_PENDING_MATCHES = "/match/list/my/pending";
    public static final String OPEN_MATCHES_FOR_JUDGE = "/match/judge/list/open";
    public static final String COMPLETE_MATCHES = "/match/list/completed/";
    public static final String COMPLETE_MATCH = "/match/participant/score";
    public static final String MATCH_WATCH_LIST_OPEN = "/match/watch/list/open";

    @Inject
    private AuthService authService;

    @Inject
    private MatchDao matchDao;

    @Inject
    private MatchService matchService;

    @GET
    @Path(MY_PENDING_MATCHES)
    @Consumes(APPLICATION_JSON)
    public List<MyPendingMatch> findPendingMatches(
            @HeaderParam(SESSION) String session) {
        final int uid = authService.userInfoBySession(session).getUid();
        return matchDao.findPendingMatches(uid);
    }

    @Inject
    private Clocker clocker;

    @Inject
    private TournamentAccessor tournamentAccessor;

    @POST
    @Path(COMPLETE_MATCH)
    @Consumes(APPLICATION_JSON)
    public void scoreSetAndCompleteIfWinOrLose(
            @Suspended AsyncResponse response,
            @HeaderParam(SESSION) String session,
            FinalMatchScore score) {
        //response.setTimeout(30, TimeUnit.SECONDS);
        final int uid = authService.userInfoBySession(session).getUid();
        log.info("User {} sets scores {} for match {}",
                uid, score.getScores(), score.getMid());
        tournamentAccessor.update(new Tid(score.getTid()), response,
                (tournament, batch) -> {
                    return matchService.scoreSet(tournament, uid, score, clocker.get(), batch);
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
    @Path(OPEN_MATCHES_FOR_JUDGE)
    @Consumes(APPLICATION_JSON)
    public List<OpenMatchForJudge> findOpenMatchesForJudge(
            @HeaderParam(SESSION) String session) {
        final int uid = authService.userInfoBySession(session).getUid();
        return matchDao.findOpenMatchesFurJudge(uid);
    }

    @GET
    @Path(MATCH_WATCH_LIST_OPEN + "/{tid}")
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
