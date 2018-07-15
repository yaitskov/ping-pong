package org.dan.ping.pong.app.tournament;

import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.tournament.console.TournamentRelationType;
import org.dan.ping.pong.sys.db.DbUpdater;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

public interface TournamentDao {
    void setState(TournamentMemState tournament, DbUpdater batch);

    void setCompleteAt(Tid tid, Optional<Instant> now, DbUpdater batch);

    Set<Uid> loadAdmins(Tid tid);

    Optional<TournamentRow> getRow(Tid tid);

    void createRelation(Tid tid, Tid consoleTid, TournamentRelationType type);

    void updateParams(Tid tid, TournamentRules rules, DbUpdater batch);
}
