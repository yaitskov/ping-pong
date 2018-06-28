package org.dan.ping.pong.mock;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.dan.ping.pong.app.auth.AuthResource.AUTH_GENERATE_SIGN_IN_LINK;
import static org.dan.ping.pong.app.bid.BidResource.BID_PAID;
import static org.dan.ping.pong.app.bid.BidResource.BID_READY_TO_PLAY;
import static org.dan.ping.pong.app.match.MatchResource.OPEN_MATCHES_FOR_JUDGE;
import static org.dan.ping.pong.app.tournament.TournamentResource.BEGIN_TOURNAMENT;
import static org.dan.ping.pong.app.tournament.TournamentResource.TOURNAMENT_CONSOLE_CREATE;
import static org.dan.ping.pong.app.tournament.TournamentResource.TOURNAMENT_ENLIST;
import static org.dan.ping.pong.app.tournament.TournamentResource.TOURNAMENT_EXPEL;

import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.bid.BidId;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.app.match.OpenMatchForJudgeList;
import org.dan.ping.pong.app.tournament.EnlistTournament;
import org.dan.ping.pong.app.tournament.ExpelParticipant;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.mock.simulator.EnlistMode;
import org.dan.ping.pong.mock.simulator.ProvidedRank;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

public class RestEntityGenerator {
    @Inject
    private MyRest rest;

    @Inject
    private DaoEntityGenerator daoEntityGenerator;

    public void enlistParticipants(SessionAware adminSession,
            Tid tid, int cid, List<TestUserSession> participants) {
        participantsEnlistThemselves(rest, adminSession, tid, cid, participants);
    }

    public void participantsEnlistThemselves(MyRest myRest, SessionAware adminSession,
            Tid tid, int cid, List<TestUserSession> participants) {
        for (int i = 0; i < participants.size(); ++i) {
            TestUserSession userSession = participants.get(i);
            Bid bid = myRest.post(TOURNAMENT_ENLIST, userSession,
                    EnlistTournament.builder()
                            .tid(tid)
                            .categoryId(cid)
                            .providedRank(empty())
                            .build()).readEntity(Bid.class);
            userSession.setBid(bid);
            myRest.voidPost(BID_PAID, adminSession,
                    BidId.builder()
                            .tid(tid)
                            .bid(userSession.getBid())
                            .build());
            myRest.voidPost(BID_READY_TO_PLAY, adminSession,
                    BidId.builder()
                            .tid(tid)
                            .bid(userSession.getBid())
                            .build());
        }
    }

    private Optional<Integer> getProvidedRank(List<Optional<ProvidedRank>> ranks, int i) {
        return ranks == null
                ? empty()
                : ranks.get(i).map(ProvidedRank::getValue);
    }

    public void enlistParticipants(MyRest myRest, SessionAware adminSession,
            Map<TestUserSession, EnlistMode> enlistModes,
            Tid tid, int cid, List<TestUserSession> participants,
            List<Optional<ProvidedRank>> ranks) {
        for (int i = 0; i < participants.size(); ++i) {
            TestUserSession userSession = participants.get(i);
            EnlistMode enlistMode = ofNullable(enlistModes.get(userSession)).orElse(EnlistMode.Pass);
            if (enlistMode.compareTo(EnlistMode.Enlist) >= 0) {
                final Bid bid = myRest.post(TOURNAMENT_ENLIST, userSession,
                        EnlistTournament.builder()
                                .tid(tid)
                                .categoryId(cid)
                                .providedRank(getProvidedRank(ranks, i))
                                .build())
                        .readEntity(Bid.class);
                userSession.setBid(bid);
            }
            if (enlistMode.compareTo(EnlistMode.Pay) >= 0) {
                myRest.voidPost(BID_PAID, adminSession,
                        BidId.builder()
                                .tid(tid)
                                .bid(userSession.getBid())
                                .build());
            }
            if (enlistMode.compareTo(EnlistMode.Pass) >= 0) {
                myRest.voidPost(BID_READY_TO_PLAY, adminSession,
                        BidId.builder()
                                .tid(tid)
                                .bid(userSession.getBid())
                                .build());
            }
            if (enlistMode == EnlistMode.Expel) {
                myRest.voidPost(TOURNAMENT_EXPEL, adminSession,
                        ExpelParticipant.builder()
                                .tid(tid)
                                .bid(userSession.getBid())
                                .targetBidState(BidState.Expl)
                                .build());
            }
            if (enlistMode == EnlistMode.Quit) {
                myRest.voidPost(TOURNAMENT_EXPEL, adminSession,
                        ExpelParticipant.builder()
                                .tid(tid)
                                .bid(userSession.getBid())
                                .targetBidState(BidState.Quit)
                                .build());
            }
        }
    }

    public void beginTournament(SessionAware testAdmin, Tid tid) {
        rest.voidPost(BEGIN_TOURNAMENT, testAdmin, tid);
    }

    public Tid createConsoleTournament(SessionAware testAdmin, Tid masterTid) {
        return rest
                .post(TOURNAMENT_CONSOLE_CREATE, testAdmin, masterTid)
                .readEntity(Tid.class);
    }

    public OpenMatchForJudgeList listOpenMatchesForJudge(Tid tid) {
        return rest.get(OPEN_MATCHES_FOR_JUDGE + tid.getTid(), OpenMatchForJudgeList.class);
    }

    public void generateSignInLinks(List<TestUserSession> users) {
        users.forEach(user
                -> rest.voidAnonymousPost(AUTH_GENERATE_SIGN_IN_LINK, user.getEmail()));
    }
}
