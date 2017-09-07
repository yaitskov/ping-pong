package org.dan.ping.pong.app.bid;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.dan.ping.pong.app.bid.BidState.Here;
import static org.dan.ping.pong.app.bid.BidState.Paid;
import static org.dan.ping.pong.app.bid.BidState.Want;
import static org.dan.ping.pong.app.bid.BidState.Win1;
import static org.dan.ping.pong.app.bid.BidState.Win2;
import static org.dan.ping.pong.app.bid.BidState.Win3;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;
import static org.dan.ping.pong.sys.error.PiPoEx.notFound;

import com.google.common.base.Preconditions;
import org.dan.ping.pong.app.tournament.DbUpdater;
import org.dan.ping.pong.app.tournament.OpenTournamentMemState;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.util.time.Clocker;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import javax.inject.Inject;

public class BidService {
    public static final List<BidState> WIN_STATES = asList(Win1, Win2, Win3);

    @Inject
    private BidDao bidDao;

    @Transactional(TRANSACTION_MANAGER)
    public void paid(OpenTournamentMemState tournament, int uid, DbUpdater batch) {
        setBidState(tournament.getParticipant(uid), Paid, singletonList(Want), batch);
    }

    @Transactional(TRANSACTION_MANAGER)
    public void readyToPlay(OpenTournamentMemState tournament, int uid, DbUpdater batch) {
        setBidState(tournament.getParticipant(uid), Here, asList(Paid, Want), batch);
    }

    public List<ParticipantState> findEnlisted(int tid) {
        return bidDao.findEnlisted(tid);
    }

    public DatedParticipantState getParticipantState(int tid, int uid) {
        return bidDao.getParticipantInfo(tid, uid)
                .orElseThrow(() -> notFound("Participant has not been found"));
    }

    @Inject
    private Clocker clocker;

    @Transactional(TRANSACTION_MANAGER)
    public void setCategory(OpenTournamentMemState tournament, SetCategory setCategory, DbUpdater batch) {
        tournament.checkCategory(setCategory.getCid());
        tournament.getParticipant(setCategory.getUid()).setCid(setCategory.getCid());
        bidDao.setCategory(setCategory, clocker.get(), batch);
    }

    public void setBidState(OpenTournamentMemState tournament, SetBidState setState, DbUpdater batch) {
        setBidState(tournament.getParticipant(setState.getUid()), setState.getTarget(),
                singletonList(setState.getExpected()), batch);
    }

    public void setBidState(ParticipantMemState bid, BidState target,
            List<BidState> expected, DbUpdater batch) {
        if (bid.getState() == target) {
            return;
        }
        if (!expected.contains(bid.getState())) {
            throw internalError(
                    "Bid " + bid.getUid() + " state "
                            + bid.getState() + " but expected " + expected);
        }
        bid.setBidState(target);
        bidDao.setBidState(bid.getTid().getTid(), bid.getUid().getId(),
                target, expected, clocker.get(), batch);
    }
}
