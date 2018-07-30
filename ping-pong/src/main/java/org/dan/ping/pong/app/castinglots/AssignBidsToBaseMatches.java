package org.dan.ping.pong.app.castinglots;

import static org.dan.ping.pong.app.tournament.ParticipantMemState.FILLER_LOSER_BID;

import org.dan.ping.pong.app.category.SelectedCid;
import org.dan.ping.pong.app.match.MatchService;
import org.dan.ping.pong.app.match.Mid;
import org.dan.ping.pong.app.tournament.ParticipantMemState;

import java.util.Optional;

import javax.inject.Inject;

public class AssignBidsToBaseMatches extends LadderSeedingAlg<ParticipantMemState> {
    @Inject
    private MatchService matchService;

    @Override
    protected void seedInMatch(
            SelectedCid sCid, Mid mid, Optional<ParticipantMemState> child) {
        matchService.assignBidToMatch(
                sCid.tournament(), mid,
                child.map(ParticipantMemState::getBid).orElse(FILLER_LOSER_BID),
                sCid.batch());

    }
}
