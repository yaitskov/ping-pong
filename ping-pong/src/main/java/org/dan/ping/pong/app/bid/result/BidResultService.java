package org.dan.ping.pong.app.bid.result;

import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import org.dan.ping.pong.app.bid.BidDao;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.tournament.OpenTournamentMemState;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.TournamentResultEntry;
import org.dan.ping.pong.app.tournament.TournamentService;
import org.dan.ping.pong.app.tournament.Uid;
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

    public BidResult getResults(OpenTournamentMemState tournament, Uid uid) {
        final ParticipantMemState participant = tournament.getParticipant(uid);
        final List<TournamentResultEntry> resultEntries = tournamentService
                .tournamentResult(tournament, participant.getCid());

        for (int iPos = 0 ;iPos < resultEntries.size(); ++iPos) {
            final TournamentResultEntry entry = resultEntries.get(iPos);
            if (entry.getUser().getUid().equals(uid)) {
                return BidResult.builder()
                        .normal(entry.getScore().getRating())
                        .user(entry.getUser())
                        .position(iPos)
                        .state(participant.getState())
                        .tournament(tournament.toLink())
                        .beated(neighbour(resultEntries, iPos + 1))
                        .conceded(neighbour(resultEntries, iPos - 1))
                        .matches(matches(tournament, uid))
                        .time(time(tournament, participant))
                        .build();
            }
        }
        throw internalError("User not found");
    }

    @Inject
    private Clocker clocker;

    private BidTimeStats time(OpenTournamentMemState tournament,
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

    private LongSummaryStatistics matchTimes(OpenTournamentMemState tournament,
            ParticipantMemState participant, Instant now) {
        return tournament.participantMatches(participant.getUid())
                .map(m -> m.duration(now))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .mapToLong(Duration::toMillis)
                .summaryStatistics();
    }

    private BidMatchesStat matches(OpenTournamentMemState tournament, Uid uid) {
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