package org.dan.ping.pong.app.bid;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.dan.ping.pong.app.auth.AuthService.SESSION;
import static org.dan.ping.pong.app.match.MatchResource.TID_JP;
import static org.dan.ping.pong.app.match.MatchResource.UID;
import static org.dan.ping.pong.app.match.MatchResource.UID_JP;
import static org.dan.ping.pong.app.tournament.TournamentService.TID;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.auth.AuthService;
import org.dan.ping.pong.app.bid.result.BidResultService;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.TournamentAccessor;

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
public class BidResource {
    private static final String BID = "/bid/";
    public static final String PROFILE = BID + "profile/";
    public static final String FIND_BIDS_BY_STATE = BID + "find-by-state";
    public static final String FIND_BIDS_WITH_MATCH = "/bid/find-with-match/";
    public static final String BID_PAID = BID + "paid";
    public static final String BID_READY_TO_PLAY = BID + "ready-to-play";
    public static final String BID_SET_STATE = BID + "set-state";
    private static final String BID_RESULTS = BID + "results/";
    public static final String TID_SLASH_UID = "/";

    @Inject
    private AuthService authService;

    @Inject
    private BidService bidService;

    @Inject
    private TournamentAccessor tournamentAccessor;

    @POST
    @Path(BID_PAID)
    @Consumes(APPLICATION_JSON)
    public void paid(
            @Suspended AsyncResponse response,
            @HeaderParam(SESSION) String session,
            BidId bidId) {
        final Uid adminUid = authService.userInfoBySession(session).getUid();
        log.info("Admin {} took money for bid {}", adminUid, bidId);
        tournamentAccessor.update(bidId.getTid(), response, (tournament, batch) -> {
            tournament.checkAdmin(adminUid);
            bidService.paid(tournament, bidId.getUid(), batch);
        });
    }

    @GET
    @Path(PROFILE + TID_JP + TID_SLASH_UID + UID_JP)
    @Consumes(APPLICATION_JSON)
    public void profile(
            @Suspended AsyncResponse response,
            @HeaderParam(SESSION) String session,
            @PathParam(TID) Tid tid,
            @PathParam(UID) Uid uid) {
        tournamentAccessor.read(tid, response, tournament -> {
            final ParticipantMemState participant = tournament.getParticipant(uid);
            return BidProfile.builder()
                    .name(participant.getName())
                    .category(tournament.getCategory(participant.getCid()))
                    .state(participant.getState())
                    .enlistedAt(participant.getEnlistedAt())
                    .build();
        });
    }

    @POST
    @Path(BID_READY_TO_PLAY)
    @Consumes(APPLICATION_JSON)
    public void readyToPlay(
            @Suspended AsyncResponse response,
            @HeaderParam(SESSION) String session,
            BidId bidId) {
        final Uid adminUid = authService.userInfoBySession(session).getUid();
        log.info("Admin {} checked that bid {} arrived to tournament in good fit",
                adminUid, bidId);
        tournamentAccessor.update(bidId.getTid(), response, (tournament, batch) -> {
            bidService.readyToPlay(tournament, bidId.getUid(), batch);
        });
    }

    @POST
    @Path(BID + "set-category")
    @Consumes(APPLICATION_JSON)
    public void setCategory(
            @Suspended AsyncResponse response,
            @HeaderParam(SESSION) String session,
            SetCategory setCategory) {
        final Uid adminUid = authService.userInfoBySession(session).getUid();
        tournamentAccessor.update(setCategory.getTid(), response, (tournament, batch) -> {
            tournament.checkAdmin(adminUid);
            bidService.setCategory(tournament, setCategory, batch);
        });
    }

    @POST
    @Path(BID_SET_STATE)
    @Consumes(APPLICATION_JSON)
    public void setStatus(
            @Suspended AsyncResponse response,
            @HeaderParam(SESSION) String session,
            SetBidState setState) {
        final Uid adminUid = authService.userInfoBySession(session).getUid();
        tournamentAccessor.update(setState.getTid(), response, (tournament, batch) -> {
            tournament.checkAdmin(adminUid);
            bidService.setBidState(tournament, setState, batch);
        });
    }

    @Inject
    private BidResultService bidResultService;

    @GET
    @Path(BID_RESULTS + TID_JP + TID_SLASH_UID + UID_JP)
    public void getResults(
            @Suspended AsyncResponse response,
            @PathParam(TID) Tid tid,
            @PathParam(UID) Uid uid) {
        tournamentAccessor.read(tid, response,
                tournament -> bidResultService.getResults(tournament, uid));
    }

    @GET
    @Path(BID + "enlisted-to-be-checked/" + TID_JP)
    public List<ParticipantState> enlistedToBeChecked(
            @HeaderParam(SESSION) String session,
            @PathParam(TID) Tid tid) {
        return bidService.findEnlisted(tid);
    }

    @GET
    @Path(BID + "state/" + TID_JP + TID_SLASH_UID + UID_JP)
    public DatedParticipantState getParticipantState(
            @HeaderParam(SESSION) String session,
            @PathParam(TID) Tid tid,
            @PathParam(UID) Uid uid) {
        return bidService.getParticipantState(tid, uid);
    }

    @POST
    @Path(FIND_BIDS_BY_STATE)
    public void findByState(
            @Suspended AsyncResponse response,
            FindByState request) {
        tournamentAccessor.read(request.getTid(), response,
                tournament -> bidService.findByState(tournament, request.getStates()));
    }

    @GET
    @Path(FIND_BIDS_WITH_MATCH + TID_JP)
    public void findWithMatch(
            @Suspended AsyncResponse response,
            @PathParam(TID) Tid tid) {
        tournamentAccessor.read(tid, response, bidService::findWithMatch);
    }
}
