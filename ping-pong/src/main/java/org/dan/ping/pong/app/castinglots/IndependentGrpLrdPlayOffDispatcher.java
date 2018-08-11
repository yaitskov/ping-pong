package org.dan.ping.pong.app.castinglots;

import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import org.dan.ping.pong.app.category.SelectedCid;
import org.dan.ping.pong.app.tournament.ParticipantMemState;

import java.util.List;

import javax.inject.Inject;

public class IndependentGrpLrdPlayOffDispatcher {
    @Inject
    private IndependentGrpLrdPlayOffRebuilder rebuilder;

    @Inject
    private IndependentGrpLrdPlayOffBuilder builder;

    public void buildIndependent(SelectedCid sCid, List<ParticipantMemState> bids) {
        switch (sCid.category().getState()) {
            case Drt:
                builder.buildIndependent(sCid, bids);
                break;
            case Ply:
            case End:
                rebuilder.buildIndependent(sCid, bids);
                break;
            default:
                throw internalError("Not acceptable state "
                        + sCid.category().getState());
        }
    }
}
