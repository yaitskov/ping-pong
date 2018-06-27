package org.dan.ping.pong.app.match.dispute;

import static org.dan.ping.pong.app.match.dispute.DisputeStatus.CLAIMED;
import static org.dan.ping.pong.app.tournament.TournamentState.Open;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;

import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.sys.db.DbUpdater;
import org.dan.ping.pong.util.time.Clocker;

import java.time.Instant;
import java.util.Optional;

import javax.inject.Inject;

public class MatchDisputeService {
    @Inject
    private MatchDisputeDao matchDisputeDao;
    @Inject
    private Clocker clocker;

    public DisputeId openDispute(TournamentMemState tournament,
            DisputeClaimRequest claim, DbUpdater batch, Uid uid) {
        MatchInfo m = tournament.getMatchById(claim.getMid());
        final Bid bid = tournament.findBidByMidAndUid(m, uid);
        m.checkParticipant(bid);

        validate(tournament, claim, bid);
        final Instant now = clocker.get();
        final DisputeMemState dispute = DisputeMemState
                .builder()
                .created(now)
                .status(CLAIMED)
                .did(tournament.getNextDispute().next())
                .mid(claim.getMid())
                .plaintiff(bid)
                .plaintiffComment(claim.getComment())
                .judgeComment(Optional.empty())
                .judge(Optional.empty())
                .proposedScore(claim.getSets())
                .resolvedAt(Optional.empty())
                .build();
        matchDisputeDao.create(tournament.getTid(), dispute, batch);

        tournament.getDisputes().add(dispute);
        return dispute.getDid();
    }

    private void validate(TournamentMemState tournament,
            DisputeClaimRequest claim, Bid bid) {
        claim.getComment().filter(c -> c.length() < 40)
                .ifPresent(c -> {
                    throw badRequest("comment-longer-than", "n", 40);
                });
        tournament.checkState(Open);
        if (tournament.getDisputes().stream()
                .filter(d -> d.getPlaintiff().equals(bid)).count() > 10) {
            throw badRequest("tournament-dispute-limit");
        }
    }
}
