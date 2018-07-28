package org.dan.ping.pong.app.bid;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.dan.ping.pong.app.auth.AuthService.SESSION;
import static org.dan.ping.pong.app.match.MatchResource.BID_JP;
import static org.dan.ping.pong.app.match.MatchResource.TID_JP;
import static org.dan.ping.pong.app.tournament.ParticipantMemState.BID;
import static org.dan.ping.pong.app.tournament.TournamentMemState.TID;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.auth.AuthService;
import org.dan.ping.pong.app.bid.result.BidResultService;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.TournamentAccessor;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
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
    public static final String PROFILE = "/bid/profile/";
    public static final String FIND_BIDS_BY_STATE = "/bid/find-by-state";
    public static final String FIND_BIDS_WITH_MATCH = "/bid/find-with-match/";
    public static final String BID_PAID = "/bid/paid";
    public static final String BID_READY_TO_PLAY = "/bid/ready-to-play";
    public static final String BID_SET_STATE = "/bid/set-state";
    private static final String BID_RESULTS = "/bid/results/";
    public static final String TID_SLASH_UID = "/";
    public static final String ENLISTED_BIDS = "/bid/enlisted-to-be-checked/";
    public static final String BID_CHANGE_GROUP = "/bid/change-group";
    public static final String BID_RENAME = "/bid/rename";
    public static final String BID_SET_CATEGORY = "/bid/set-category";

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
            @NotNull @HeaderParam(SESSION) String session,
            @Valid BidId bidId) {
        final Uid adminUid = authService.userInfoBySession(session).getUid();
        log.info("Admin {} took money for bid {}", adminUid, bidId);
        tournamentAccessor.update(bidId.getTid(), response, (tournament, batch) -> {
            tournament.checkAdmin(adminUid);
            bidService.paid(tournament, bidId.getBid(), batch);
        });
    }

    @POST
    @Path(BID_RENAME)
    @Consumes(APPLICATION_JSON)
    public void rename(
            @Suspended AsyncResponse response,
            @HeaderParam(SESSION) String session,
            BidRename bidRename) {
        final Uid adminUid = authService.userInfoBySession(session).getUid();
        log.info("Admin {} renames bid {} as [{}]",
                adminUid, bidRename.getBid(), bidRename.getNewName());
        tournamentAccessor.update(bidRename.getTid(), response, (tournament, batch) -> {
            tournament.checkAdmin(adminUid);
            bidService.rename(tournament, batch, bidRename);
        });
    }

    @GET
    @Path(PROFILE + TID_JP + TID_SLASH_UID + BID_JP)
    @Consumes(APPLICATION_JSON)
    public void profile(
            @Suspended AsyncResponse response,
            @Valid @PathParam(TID) Tid tid,
            @Valid @PathParam(BID) Bid bid) {
        tournamentAccessor.read(tid, response, tournament -> {
            final ParticipantMemState participant = tournament.getParticipant(bid);
            return BidProfile.builder()
                    .name(participant.getName())
                    .category(tournament.getCategory(participant.getCid()).toLink())
                    .group(participant.getGid()
                            .map(gid -> tournament.getGroups().get(gid).toLink()))
                    .state(participant.state())
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
            bidService.readyToPlay(tournament, bidId.getBid(), batch);
        });
    }

    @POST
    @Path(BID_SET_CATEGORY)
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
    @Path(BID_CHANGE_GROUP)
    @Consumes(APPLICATION_JSON)
    public void changeGroup(
            @Suspended AsyncResponse response,
            @HeaderParam(SESSION) String session,
            ChangeGroupReq req) {
        final Uid adminUid = authService.userInfoBySession(session).getUid();
        tournamentAccessor.update(req.getTid(), response, (tournament, batch) -> {
            tournament.checkAdmin(adminUid);
            bidService.changeGroup(tournament, req, batch);
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
    @Path(BID_RESULTS + TID_JP + TID_SLASH_UID + BID_JP)
    public void getResults(
            @Suspended AsyncResponse response,
            @Valid @PathParam(TID) Tid tid,
            @Valid @PathParam(BID) Bid bid) {
        tournamentAccessor.read(tid, response,
                tournament -> bidResultService.getResults(tournament, bid));
    }

    @GET
    @Path(ENLISTED_BIDS + TID_JP)
    public void enlistedToBeChecked(
            @Suspended AsyncResponse response,
            @HeaderParam(SESSION) String session,
            @PathParam(TID) Tid tid) {
        tournamentAccessor.read(tid, response,
                tournament -> bidService.findEnlisted(tournament));
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
