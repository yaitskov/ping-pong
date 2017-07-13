package org.dan.ping.pong.mock;

import static org.dan.ping.pong.app.auth.AuthResource.AUTH_GENERATE_SIGN_IN_LINK;
import static org.dan.ping.pong.app.bid.BidResource.BID_PAID;
import static org.dan.ping.pong.app.bid.BidResource.BID_READY_TO_PLAY;
import static org.dan.ping.pong.app.castinglots.CastingLotsResource.CASTING_LOTS;
import static org.dan.ping.pong.app.match.MatchResource.COMPLETE_MATCHES;
import static org.dan.ping.pong.app.match.MatchResource.OPEN_MATCHES_FOR_JUDGE;
import static org.dan.ping.pong.app.tournament.TournamentResource.BEGIN_TOURNAMENT;
import static org.dan.ping.pong.app.tournament.TournamentResource.TOURNAMENT_ENLIST;

import org.dan.ping.pong.app.bid.BidId;
import org.dan.ping.pong.app.castinglots.DoCastingLots;
import org.dan.ping.pong.app.match.CompleteMatch;
import org.dan.ping.pong.app.match.OpenMatchForJudge;
import org.dan.ping.pong.app.tournament.EnlistTournament;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.core.GenericType;

public class RestEntityGenerator {
    @Inject
    private MyRest rest;

    @Inject
    private DaoEntityGenerator daoEntityGenerator;

    public void enlistParticipants(TestAdmin adminSession,
            int tid, int cid, List<TestUserSession> participants) {
        enlistParticipants(rest, adminSession, tid, cid, participants);
    }
    public void enlistParticipants(MyRest myRest, TestAdmin adminSession,
            int tid, int cid, List<TestUserSession> participants) {
        for (TestUserSession userSession : participants) {
            myRest.voidPost(TOURNAMENT_ENLIST, userSession,
                    EnlistTournament.builder()
                            .tid(tid)
                            .categoryId(cid)
                            .build());
            myRest.voidPost(BID_PAID, adminSession,
                    BidId.builder()
                            .tid(tid)
                            .uid(userSession.getUid())
                            .build());
            myRest.voidPost(BID_READY_TO_PLAY, adminSession,
                    BidId.builder()
                            .tid(tid)
                            .uid(userSession.getUid())
                            .build());
        }
    }

    public void makeGroup(MyRest myRest, TestAdmin adminSession, int tid) {
        myRest.voidPost(CASTING_LOTS, adminSession,
                DoCastingLots.builder().tid(tid).build());
    }

    public List<TestUserSession> generateGroupsOf(MyRest myRest, TestAdmin adminSession,
            UserSessionGenerator userSessionGenerator,
            int tid, int numberParticipants) {
        final int cid = daoEntityGenerator.genCategory(tid);
        final List<TestUserSession> participants = userSessionGenerator.generateUserSessions(numberParticipants);
        enlistParticipants(myRest, adminSession, tid, cid, participants);
        makeGroup(myRest, adminSession, tid);
        return participants;
    }

    public void beginTournament(TestAdmin testAdmin, int tid) {
        rest.voidPost(BEGIN_TOURNAMENT, testAdmin, tid);
    }

    public List<OpenMatchForJudge> listOpenMatchesForJudge(TestAdmin testAdmin) {
        return rest.get(OPEN_MATCHES_FOR_JUDGE, testAdmin, new GenericType<List<OpenMatchForJudge>>(){});
    }

    public List<CompleteMatch> listCompleteMatches(int tid) {
        return rest.get(COMPLETE_MATCHES + tid, new GenericType<List<CompleteMatch>>(){});
    }

    public void generateSignInLinks(List<TestUserSession> users) {
        users.forEach(user
                -> rest.voidAnonymousPost(AUTH_GENERATE_SIGN_IN_LINK, user.getEmail()));
    }
}
