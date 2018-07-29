package org.dan.ping.pong.app.castinglots;

import org.dan.ping.pong.app.category.SelectedCid;
import org.dan.ping.pong.app.tournament.ParticipantMemState;

import java.util.List;

public interface CategoryPlayOffBuilder {
    void build(SelectedCid sCid, List<ParticipantMemState> bids);
}
