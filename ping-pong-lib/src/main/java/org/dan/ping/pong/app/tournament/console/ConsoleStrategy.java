package org.dan.ping.pong.app.tournament.console;

import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.category.Cid;
import org.dan.ping.pong.app.group.Gid;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.sys.db.DbUpdater;

import java.util.Set;

public interface ConsoleStrategy {
    void onGroupComplete(Gid gid, TournamentMemState tournament,
            Set<Bid> loserBids, DbUpdater batch);

    void onPlayOffCategoryComplete(Cid cid, TournamentMemState tournament, DbUpdater batch);

    void onParticipantLostPlayOff(
            TournamentMemState tournament,
            ParticipantMemState lostParticipant,
            DbUpdater batch);
}
