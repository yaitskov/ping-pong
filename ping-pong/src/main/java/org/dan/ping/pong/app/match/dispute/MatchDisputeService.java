package org.dan.ping.pong.app.match.dispute;

import static org.dan.ping.pong.app.match.dispute.DisputeStatus.CLAIMED;
import static org.dan.ping.pong.app.tournament.TournamentState.Open;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;

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
        validate(tournament, claim, uid);
        final Instant now = clocker.get();
        final DisputeMemState dispute = DisputeMemState
                .builder()
                .created(now)
                .status(CLAIMED)
                .mid(claim.getMid())
                .plaintiff(uid)
                .plaintiffComment(claim.getComment())
                .judgeComment(Optional.empty())
                .judge(Optional.empty())
                .proposedScore(claim.getSets())
                .resolvedAt(Optional.empty())
                .build();
        batch.markDirty();
        dispute.setDid(matchDisputeDao.create(tournament.getTid(), dispute));

        tournament.getDisputes().add(dispute);
        return dispute.getDid();
    }

    private void validate(TournamentMemState tournament,
            DisputeClaimRequest claim, Uid uid) {
        claim.getComment().filter(c -> c.length() < 40)
                .ifPresent(c -> {
                    throw badRequest("comment-longer-than", "n", 40);
                });
        tournament.getParticipant(uid);
        tournament.checkState(Open);
        MatchInfo m = tournament.getMatchById(claim.getMid());
        m.checkParticipant(uid);

        if (tournament.getDisputes().stream()
                .filter(d -> d.getPlaintiff().equals(uid)).count() > 10) {
            throw badRequest("tournament-dispute-limit");
        }
    }
}
