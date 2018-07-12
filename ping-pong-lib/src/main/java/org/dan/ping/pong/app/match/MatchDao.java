package org.dan.ping.pong.app.match;

import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.category.Cid;
import org.dan.ping.pong.app.group.Gid;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.sys.db.DbUpdater;
import org.dan.ping.pong.util.collection.MaxValue;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MatchDao {
    void createMatch(MatchInfo matchInfo, DbUpdater dbStrictUpdater);

    void createGroupMatch(DbUpdater batch, Mid mid, Tid tid, Gid gid, Cid cid, int priorityGroup,
            Bid bid1, Bid bid2, Optional<MatchTag> tag, MatchState place);

    void  createPlayOffMatch(DbUpdater batch, Mid mid, Tid tid, Cid cid,
            Optional<Mid> winMid, Optional<Mid> loseMid,
            int priority, int level, MatchType type, Optional<MatchTag> tag, MatchState draft);

    void changeStatus(MatchInfo mInfo, DbUpdater batch);

    void scoreSet(TournamentMemState tournament, MatchInfo matchInfo,
            DbUpdater batch, List<IdentifiedScore> scores);

    void completeMatch(MatchInfo mInfo, DbUpdater batch, Set<MatchState> expected);

    void markAsSchedule(MatchInfo match, DbUpdater batch);

    void deleteAllByTid(TournamentMemState tournament, DbUpdater batch, int size);

    void deleteByIds(Tid tid, Collection<Mid> mids, DbUpdater batch);

    void setParticipant(MatchInfo mInfo, Bid bid, DbUpdater batch);

    List<MatchInfo> load(Tid tid, MaxValue<Mid> maxMid);

    void deleteSets(DbUpdater batch, MatchInfo mInfo, int setNumber);

    void removeSecondParticipant(DbUpdater batch, MatchInfo mInfo, Bid uidKeep);

    void removeParticipants(DbUpdater batch, MatchInfo mInfo);

    void removeScores(DbUpdater batch, MatchInfo mInfo, Bid bid, int played);

    void setWinnerId(MatchInfo mInfo, DbUpdater batch);

    void insertScores(MatchInfo mInfo, DbUpdater batch);
}
