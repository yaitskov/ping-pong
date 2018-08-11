package org.dan.ping.pong.app.match;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Comparator.comparing;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;
import static org.dan.ping.pong.app.match.MatchBid.toEffectedMatchesByRemove;
import static org.dan.ping.pong.app.match.MatchBid.toEffectedMatchesByReset;
import static org.dan.ping.pong.app.match.NewEffectedMatch.toNewDisambiguationMatches;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.dan.ping.pong.sys.hash.HashAggregator;
import org.dan.ping.pong.sys.hash.Hashable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Getter
@Setter
@Builder
public class AffectedMatches implements Hashable {
    private final Tid tid;
    // play off matches
    private List<MatchBid> toBeReset;
    // disambiguation matches only
    private Set<Mid> toBeRemovedDm;
    // disambiguation matches only
    private Set<MatchParticipants> toBeCreatedDm;
    // in console only
    private Optional<LineUpDiff> lineUpDiff;

    private Map<Tid, AffectedMatches> consoleAffect;

    public static class AffectedMatchesBuilder {
        List<ParticipantReplace> toBeReplacedInCon = emptyList();
        List<MatchBid> toBeReset = emptyList();
        Set<Mid> toBeRemovedDm = emptySet();
        Set<MatchParticipants> toBeCreatedDm = emptySet();
        Map<Tid, AffectedMatches> consoleAffect = emptyMap();
        Optional<LineUpDiff> lineUpDiff = empty();
    }

    public static final AffectedMatches NO_AFFECTED_MATCHES = AffectedMatches
            .builder().build();

    public static AffectedMatches ofResets(List<MatchBid> toBeReset) {
        return AffectedMatches.builder().toBeReset(toBeReset).build();
    }

    public AffectedMatches deduplicate() {
        toBeReset = toBeReset.stream()
                .sorted(comparing(MatchBid::getMid)
                        .thenComparing(MatchBid::getBid))
                .distinct() // uids can meet which cause some matches to be processed twice
                .collect(toList());
        return this;
    }

    @Override
    public void hashTo(HashAggregator sink) {
        sink.section("tid").hash(tid);
        sink.section("reset");
        toBeReset.stream().sorted().forEach(sink::hash);
        sink.section("remove");
        toBeRemovedDm.stream().sorted().forEach(sink::hash);
        sink.section("create");
        toBeCreatedDm.stream().sorted().forEach(sink::hash);

        if (consoleAffect.isEmpty()) {
            return;
        }
        sink.section("console");
        consoleAffect.values().stream()
                .sorted(comparing(AffectedMatches::getTid))
                .forEachOrdered(sink::hash);
    }

    public EffectHashMismatchError createError(
            TournamentMemState tournament, Optional<String> expectedEffectHash) {
        return new EffectHashMismatchError(expectedEffectHash,
                toEffectedMatchesByReset(tournament, toBeReset),
                toNewDisambiguationMatches(tournament, toBeCreatedDm),
                toEffectedMatchesByRemove(tournament, toBeRemovedDm));
    }
}
