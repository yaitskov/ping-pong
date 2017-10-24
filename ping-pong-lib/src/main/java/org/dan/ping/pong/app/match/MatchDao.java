package org.dan.ping.pong.app.match;

import org.dan.ping.pong.app.tournament.OpenTournamentMemState;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.Uid;
import org.dan.ping.pong.app.user.UserLink;
import org.dan.ping.pong.sys.db.DbUpdater;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface MatchDao {
    int createGroupMatch(int tid, int gid, int cid, int priorityGroup, Uid uid1, Uid uid2);

    int createPlayOffMatch(int tid, Integer cid,
            Optional<Integer> winMid, Optional<Integer> loseMid,
            int priority, int level, MatchType type);

    List<OpenMatchForJudge> findOpenMatchesFurJudge(Uid adminUid);

    void changeStatus(int mid, MatchState state, DbUpdater batch);

    Optional<Uid> scoreSet(OpenTournamentMemState tournament, MatchInfo matchInfo,
            DbUpdater batch, FinalMatchScore matchScore);

    void completeMatch(int mid, Uid winUid, Instant now, DbUpdater batch, MatchState... expected);

    void markAsSchedule(MatchInfo match, DbUpdater batch);

    List<CompleteMatch> findCompleteMatches(Integer tid);

    List<UserLink> findWinners(int tid);

    void deleteAllByTid(OpenTournamentMemState tournament, DbUpdater batch, int size);

    void setParticipant(int n, int mid, Uid uid, DbUpdater batch);

    List<MatchInfo> load(Tid tid);

    void deleteSets(DbUpdater batch, MatchInfo minfo, int setNumber);

    void removeSecondParticipant(DbUpdater batch, int mid, Uid uidKeep);

    void removeParticipants(DbUpdater batch, int mid);

    void removeScores(DbUpdater batch, int mid, Uid uid);
}
