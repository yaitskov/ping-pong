package org.dan.ping.pong.app.match;

import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.user.UserLink;
import org.dan.ping.pong.sys.db.DbUpdater;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MatchDao {
    Mid createGroupMatch(Tid tid, int gid, int cid, int priorityGroup, Uid uid1, Uid uid2);

    Mid createPlayOffMatch(Tid tid, Integer cid,
            Optional<Mid> winMid, Optional<Mid> loseMid,
            int priority, int level, MatchType type);

    void changeStatus(Mid mid, MatchState state, DbUpdater batch);

    Optional<Uid> scoreSet(TournamentMemState tournament, MatchInfo matchInfo,
            DbUpdater batch, List<IdentifiedScore> scores);

    void completeMatch(Mid mid, Uid winUid, Instant now, DbUpdater batch, Set<MatchState> expected);

    void markAsSchedule(MatchInfo match, DbUpdater batch);

    List<CompleteMatch> findCompleteMatches(Tid tid);

    List<UserLink> findWinners(Tid tid);

    void deleteAllByTid(TournamentMemState tournament, DbUpdater batch, int size);

    void setParticipant(int n, Mid mid, Uid uid, DbUpdater batch);

    List<MatchInfo> load(Tid tid);

    void deleteSets(DbUpdater batch, MatchInfo minfo, int setNumber);

    void removeSecondParticipant(DbUpdater batch, Mid mid, Uid uidKeep);

    void removeParticipants(DbUpdater batch, Mid mid);

    void removeScores(DbUpdater batch, Mid mid, Uid uid);

    void setWinnerId(MatchInfo mInfo, DbUpdater batch);

    void insertScores(MatchInfo mInfo, DbUpdater batch);
}
