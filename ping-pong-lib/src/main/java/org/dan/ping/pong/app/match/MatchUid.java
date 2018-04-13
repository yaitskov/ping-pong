package org.dan.ping.pong.app.match;

import static java.util.stream.Collectors.toList;
import static org.dan.ping.pong.app.match.EffectedMatch.ofMatchInfo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.sys.hash.HashAggregator;
import org.dan.ping.pong.sys.hash.Hashable;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class MatchUid implements Comparable<MatchUid>, Hashable {
    private static final Comparator<MatchUid> COMPARATOR = Comparator
            .comparing(MatchUid::getMid)
            .thenComparing(MatchUid::getUid);

    private Mid mid;
    private Uid uid;

    @Override
    public int compareTo(MatchUid o) {
        return COMPARATOR.compare(this, o);
    }

    @Override
    public void hashTo(HashAggregator sink) {
        sink.hash(uid).hash(mid);
    }

    public static List<EffectedMatch> toEffectedMatchesByReset(
            TournamentMemState tournament, List<MatchUid> toBeReset) {
        return toBeReset.stream()
                .map(MatchUid::getMid)
                .sorted()
                .distinct()
                .map(tournament::getMatchById)
                .sorted(Comparator.comparing(MatchInfo::getLevel))
                .map(m -> ofMatchInfo(tournament, m))
                .collect(toList());
    }

    public static List<EffectedMatch> toEffectedMatchesByRemove(
            TournamentMemState tournament, Collection<Mid> toBeRemoved) {
        return toBeRemoved.stream()
                .map(tournament::getMatchById)
                .sorted(Comparator.comparing(MatchInfo::getMid))
                .map(m -> ofMatchInfo(tournament, m))
                .collect(toList());
    }
}
