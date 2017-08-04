package org.dan.ping.pong.app.match;

import static java.util.Arrays.asList;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.dan.ping.pong.app.bid.BidState.Rest;
import static org.dan.ping.pong.app.match.MatchState.Game;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;
import static org.dan.ping.pong.sys.error.PiPoEx.forbidden;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;
import static org.dan.ping.pong.sys.error.PiPoEx.notFound;
import static org.dan.ping.pong.app.bid.BidState.Lost;
import static org.dan.ping.pong.app.bid.BidState.Play;
import static org.dan.ping.pong.app.bid.BidState.Wait;
import static org.dan.ping.pong.app.bid.BidState.Win1;
import static org.dan.ping.pong.app.bid.BidState.Win2;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;
import static org.dan.ping.pong.app.match.MatchState.Over;
import static org.dan.ping.pong.util.Integers.odd;
import static org.dan.ping.pong.util.collection.Enumerator.backward;
import static org.dan.ping.pong.util.collection.Enumerator.forward;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.BidDao;
import org.dan.ping.pong.app.bid.BidId;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.app.group.GroupDao;
import org.dan.ping.pong.app.group.GroupInfo;
import org.dan.ping.pong.app.table.TableDao;
import org.dan.ping.pong.app.table.TableService;
import org.dan.ping.pong.app.tournament.PlayOffMatchForResign;
import org.dan.ping.pong.app.tournament.TournamentDao;
import org.dan.ping.pong.app.tournament.TournamentInfo;
import org.dan.ping.pong.util.time.Clocker;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;

import javax.inject.Inject;

@Slf4j
public class MatchService {
    @Inject
    private MatchDao matchDao;

    @Inject
    private TournamentDao tournamentDao;

    @Inject
    private Clocker clocker;

    @Inject
    private TableDao tableDao;

    @Inject
    private TableService tableService;

    @Inject
    private BidDao bidDao;

    @Transactional(TRANSACTION_MANAGER)
    public void complete(int uid, FinalMatchScore score, Instant now) {
        final MatchInfo matchInfo = matchDao.getById(score.getMid())
                .orElseThrow(() -> notFound("Match "
                        + score.getMid() + " doesn't exist"));
        checkPermissions(uid, matchInfo, score);
        matchDao.complete(clocker.get(), score);
        tableDao.freeTable(score.getMid());

        if (matchInfo.getGid().isPresent()) {
            tryToCompleteGroup(matchInfo.getGid().get(),
                    matchInfo.getTid(), matchInfo.getParticipantIds());
        } else {
            completePlayOffMatch(matchInfo, score);
        }
        tableService.scheduleFreeTables(matchInfo.getTid(), now);
    }

    public boolean completePlayOffMatch(int uid, int tid, BidState target, PlayOffMatchForResign match) {
        matchDao.complete(uid, match.getOpponentId(), match.getMid());
        bidDao.setBidState(BidId.builder().tid(tid).uid(uid).build(),
                asList(Play, Rest, Wait), target);
        if (match.getWinMatch().isPresent()) {
            if (match.getOpponentId().isPresent()) {
                matchDao.assignMatchForWinner(tid, match.getWinMatch().get(), match.getOpponentId().get());
                if (match.getState() == Game) {
                    bidDao.setBidState(tid, match.getOpponentId().get(), Play, Wait);
                }
            } else {
                // should be covered in other place where the opponent is detected
            }

            if (match.getLostMatch().isPresent()) {
                matchDao.resignFromMatchForLoser(tid, uid, match.getLostMatch().get());
            }
            return true;
        } else {
            return endOfTournamentCategory(tid);
        }
    }

    private boolean completePlayOffMatch(MatchInfo matchInfo, FinalMatchScore score) {
        final int winnerId = score.getScores().stream()
                .max(comparingInt(IdentifiedScore::getScore))
                .orElseThrow(() -> internalError("no scores"))
                .getUid();
        if (matchInfo.getWinnerMid().isPresent()) {
            matchInfo.getParticipantIds().forEach(bid ->
                    bidDao.setBidState(matchInfo.getTid(), bid, Play,
                            winnerId == bid ? Wait : Lost));
            matchDao.assignMatchForWinner(winnerId, matchInfo);
            return true;
        } else {
            matchInfo.getParticipantIds().forEach(bid ->
                    bidDao.setBidState(matchInfo.getTid(), bid, Play,
                            winnerId == bid ? Win1 : Win2));
            return endOfTournamentCategory(matchInfo.getTid());
        }
    }

    private boolean endOfTournamentCategory(int tid) {
        log.info("Tid {} complete in cid {}", tid);
        if (tournamentDao.tryToCompleteTournament(tid)) {
            log.info("Tid {} is fully complete", tid);
            return false;
        }
        return true;
    }

    @Inject
    private GroupDao groupDao;

    public void tryToCompleteGroup(Integer gid, int tid, List<Integer> uids) {
        uids.forEach(bid -> bidDao.setBidState(tid, bid, Play, Wait));

        final List<GroupMatchInfo> matches = matchDao.findMatchesInGroup(gid);
        final long completedMatches = matches.stream()
                .map(GroupMatchInfo::getState)
                .filter(Over::equals)
                .count();
        if (completedMatches < matches.size()) {
            log.debug("Matches {} left to play in the group {}",
                    matches.size() - completedMatches, gid);
            return;
        }
        final Set<Integer> winnerIds = completeGroup(gid, tid, matches);
        bidDao.setStatesAfterGroup(gid, tid, winnerIds);
    }

    private Set<Integer> completeGroup(Integer gid, int tid,
            List<GroupMatchInfo> matches) {
        log.info("Pick bids for playoff from gid {} in tid {}", gid, tid);
        final TournamentInfo iTour = tournamentDao.getById(tid)
                .orElseThrow(() -> internalError("tid " + tid + " disappeared"));
        Map<Optional<Integer>, List<GroupMatchInfo>> byWinner = matches.stream()
                .collect(groupingBy(GroupMatchInfo::getWinnerId, toList()));
        final PriorityQueue<List<GroupMatchInfo>> pq = new PriorityQueue<>(
                GroupMatchListComparator.COMPARATOR);
        pq.addAll(byWinner.values());
        final GroupInfo iGru = groupDao.getById(gid).orElseThrow(
                () -> internalError("Group " + gid + " disappeared"));
        final int playOffMatchOffset = iGru.getOrdNumber() / 2
                * iTour.getQuitesFromGroup();
        final List<PlayOffMatchInfo> playOffMatches = matchDao.findPlayOffMatches(
                tid, iGru.getCid());
        if (playOffMatches.isEmpty()) {
            if (iTour.getQuitesFromGroup() == 1 && iGru.getOrdNumber() == 0) {
                final int winnerId = pq.poll().get(0).getWinnerId().get();
                log.info("1 group tid {} and no play off won {}", iTour.getTid(), winnerId);
                bidDao.setBidState(iTour.getTid(), winnerId, Wait, Win1);
                endOfTournamentCategory(iTour.getTid());
                return Collections.singleton(winnerId);
            } else {
                throw internalError("Tournament " +
                        iTour.getTid() + " in category " + iGru.getCid()
                        + " doesn't have playoff matches");
            }
        }
        final Iterable<Integer> matchIndexes = odd(iGru.getOrdNumber())
                ? backward(iTour.getQuitesFromGroup(), 0)
                : forward(0, iTour.getQuitesFromGroup());
        final Set<Integer> winnerIds = new HashSet<>();
        for (int i : matchIndexes) {
            List<GroupMatchInfo> wonMatches = pq.poll();
            if (wonMatches == null) {
                log.error("Not enough winners in group {}; idx = {}", gid, i);
                break;
            }
            final int winnerId = wonMatches.get(0).getWinnerId().get();
            winnerIds.add(winnerId);
            log.info("uid {} quits out of gid {}", winnerId, gid);
            matchDao.goPlayOff(winnerId,
                    playOffMatches.get((playOffMatchOffset + i)
                            % playOffMatches.size()));
        }
        return winnerIds;
    }

    private void checkPermissions(int senderUid, MatchInfo matchInfo,
            FinalMatchScore score) {
        final Set<Integer> scoreUids = score.getScores().stream()
                .map(IdentifiedScore::getUid).collect(toSet());
        final Set<Integer> participantIds = new HashSet<>(
                matchInfo.getParticipantIds());
        if (!participantIds.equals(scoreUids)) {
            throw badRequest("Some of participants don't play match "
                    + matchInfo.getMid());
        }
        if (!participantIds.contains(senderUid)
                && !tournamentDao.isAdminOf(senderUid, matchInfo.getTid())) {
            throw forbidden("User " + senderUid
                    + " neither admin of the tournament"
                    + " nor a participant of the match");
        }
        if (matchInfo.getState() == Over && participantIds.contains(senderUid)) {
            throw forbidden("User cannot overwrite admin's result");
        }
    }
}
