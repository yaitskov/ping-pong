package org.dan.ping.pong.app.bid.result;

import org.dan.ping.pong.app.bid.BidDao;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.TournamentResultEntry;
import org.dan.ping.pong.app.tournament.TournamentService;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.user.UserLink;
import org.dan.ping.pong.util.collection.CounterInt;
import org.dan.ping.pong.util.time.Clocker;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Optional;

import javax.inject.Inject;

public class BidResultService {
    @Inject
    private TournamentService tournamentService;

    public BidResult getResults(TournamentMemState tournament, Uid uid) {
        final ParticipantMemState participant = tournament.getParticipant(uid);
        final List<TournamentResultEntry> resultEntries = tournamentService
                .tournamentResult(tournament, participant.getCid());

        for (int iPos = 0 ;iPos < resultEntries.size(); ++iPos) {
            final TournamentResultEntry entry = resultEntries.get(iPos);
            if (entry.getUser().getUid().equals(uid)) {
                return BidResult.builder()
                        .user(entry.getUser())
                        .position(Optional.of(iPos))
                        .state(participant.getState())
                        .tournament(tournament.toLink())
                        .beated(neighbour(resultEntries, iPos + 1))
                        .conceded(neighbour(resultEntries, iPos - 1))
                        .matches(Optional.of(matches(tournament, uid)))
                        .time(Optional.of(time(tournament, participant)))
                        .build();
            }
        }
        return BidResult.builder()
                .normal(Optional.empty())
                .user(participant.toLink())
                .position(Optional.empty())
                .state(participant.getState())
                .tournament(tournament.toLink())
                .beated(Optional.empty())
                .conceded(Optional.empty())
                .matches(Optional.empty())
                .time(Optional.empty())
                .build();
    }

    @Inject
    private Clocker clocker;

    private BidTimeStats time(TournamentMemState tournament,
            ParticipantMemState participant) {
        final Instant now = clocker.get();
        final LongSummaryStatistics stat = matchTimes(tournament, participant, now);
        return BidTimeStats.builder()
                .enlistedAt(participant.getEnlistedAt())
                .completeAt(BidDao.TERMINAL_BID_STATES.contains(participant.getState())
                        ? Optional.of(participant.getUpdatedAt())
                        : Optional.empty())
                .totalMs(stat.getSum())
                .avgMs((long) stat.getAverage())
                .build();
    }

    private LongSummaryStatistics matchTimes(TournamentMemState tournament,
            ParticipantMemState participant, Instant now) {
        return tournament.participantMatches(participant.getUid())
                .map(m -> m.duration(now))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .mapToLong(Duration::toMillis)
                .summaryStatistics();
    }

    private BidMatchesStat matches(TournamentMemState tournament, Uid uid) {
        final CounterInt total = new CounterInt();
        final CounterInt won = new CounterInt();
        final CounterInt lost = new CounterInt();
        tournament.participantMatches(uid)
                .peek(m -> total.inc())
                .map(MatchInfo::getWinnerId)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(winId -> (winId == uid ? won : lost).inc());

        return BidMatchesStat.builder()
                .total(total.toInt())
                .lost(lost.toInt())
                .won(won.toInt())
                .build();
    }

    private Optional<UserLink> neighbour(
            List<TournamentResultEntry> resultEntries, int iPos) {
        if (iPos < 0 || iPos >= resultEntries.size()) {
            return Optional.empty();
        }
        return Optional.of(resultEntries.get(iPos).getUser());
    }
}
