package org.dan.ping.pong.app.match;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
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
import org.dan.ping.pong.app.group.GroupDao;
import org.dan.ping.pong.app.group.GroupInfo;
import org.dan.ping.pong.app.table.TableDao;
import org.dan.ping.pong.app.table.TableService;
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

        boolean schedule = true;
        if (matchInfo.getGid().isPresent()) {
            tryToCompleteGroup(matchInfo.getGid().get(), matchInfo);
        } else {
             schedule = completePlayOffMatch(matchInfo, score);
        }
        if (schedule) {
            tableService.scheduleFreeTables(matchInfo.getTid(), now);
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

    private void tryToCompleteGroup(Integer gid, MatchInfo matchInfo) {
        final List<GroupMatchInfo> matches = matchDao.findMatchesInGroup(gid);
        final long completedMatches = matches.stream()
                .map(GroupMatchInfo::getState)
                .filter(Over::equals)
                .count();
        matchInfo.getParticipantIds().forEach(bid ->
                bidDao.setBidState(matchInfo.getTid(), bid, Play, Wait));
        if (completedMatches < matches.size()) {
            log.debug("Matches {} left to play in the group {}",
                    matches.size() - completedMatches, gid);
            return;
        }
        final Set<Integer> winnerIds = completeGroup(gid, matchInfo, matches);
        bidDao.setStatesAfterGroup(gid, matchInfo.getTid(), winnerIds);
    }

    private Set<Integer> completeGroup(Integer gid, MatchInfo matchInfo,
            List<GroupMatchInfo> matches) {
        log.info("Pick bids for playoff from gid {} in tid {} ",
                gid, matchInfo.getTid());
        final TournamentInfo iTour = tournamentDao.getById(matchInfo.getTid())
                .orElseThrow(() -> internalError("tid "
                        + matchInfo.getTid() + " disappeared"));
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
                matchInfo.getTid(), iGru.getCid());
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
            final int winnerId = pq.poll().get(0).getWinnerId().get();
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
