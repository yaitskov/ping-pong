package org.dan.ping.pong.app.match;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;
import static org.dan.ping.pong.app.match.MatchUid.toEffectedMatchesByRemove;
import static org.dan.ping.pong.app.match.MatchUid.toEffectedMatchesByReset;
import static org.dan.ping.pong.app.match.NewEffectedMatch.toNewDisambiguationMatches;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.sys.hash.HashAggregator;
import org.dan.ping.pong.sys.hash.Hashable;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Getter
@Setter
@Builder
public class AffectedMatches implements Hashable {
    // play off matches
    private List<MatchUid> toBeReset;
    // disambiguation matches only
    private Set<Mid> toBeRemoved;
    // disambiguation matches only
    private Set<MatchParticipants> toBeCreated;

    public static final AffectedMatches NO_AFFECTED_MATCHES = AffectedMatches
            .builder()
            .toBeReset(emptyList())
            .toBeRemoved(emptySet())
            .toBeCreated(emptySet())
            .build();

    public static AffectedMatches ofResets(List<MatchUid> toBeReset) {
        return AffectedMatches
                .builder()
                .toBeReset(toBeReset)
                .toBeRemoved(emptySet())
                .toBeCreated(emptySet())
                .build();
    }

    public AffectedMatches deduplicate() {
        toBeReset = toBeReset.stream()
                .sorted(Comparator.comparing(MatchUid::getMid)
                        .thenComparing(MatchUid::getUid))
                .distinct() // uids can meet which cause some matches to be processed twice
                .collect(toList());
        return this;
    }

    @Override
    public void hashTo(HashAggregator sink) {
        sink.section("reset");
        toBeReset.stream().sorted().forEach(sink::hash);
        sink.section("remove");
        toBeRemoved.stream().sorted().forEach(sink::hash);
        sink.section("create");
        toBeCreated.stream().sorted().forEach(sink::hash);
    }

    public EffectHashMismatchError createError(
            TournamentMemState tournament, Optional<String> expectedEffectHash) {
        return new EffectHashMismatchError(expectedEffectHash,
                toEffectedMatchesByReset(tournament, toBeReset),
                toNewDisambiguationMatches(tournament, toBeCreated),
                toEffectedMatchesByRemove(tournament, toBeRemoved));
    }
}
