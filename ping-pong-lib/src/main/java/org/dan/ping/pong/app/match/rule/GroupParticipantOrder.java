package org.dan.ping.pong.app.match.rule;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

import com.google.common.collect.ImmutableMap;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.match.rule.filter.DisambiguationScope;
import org.dan.ping.pong.app.match.rule.filter.MatchOutcomeScope;
import org.dan.ping.pong.app.match.rule.filter.MatchParticipantScope;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

@Getter
@Setter
@Builder
public class GroupParticipantOrder {
    private TreeMap<GroupPositionIdx, GroupPosition> positions;
    private Set<GroupPositionIdx> ambiguousPositions;

    public boolean unambiguous() {
        return ambiguousPositions.isEmpty();
    }

    public static GroupParticipantOrder orderOf(Set<Uid> uids) {
        final GroupPositionIdx zeroPos = new GroupPositionIdx(0);
        return GroupParticipantOrder.builder()
                .positions(new TreeMap<>(ImmutableMap.of(zeroPos, GroupPosition
                        .builder()
                        .competingUids(uids)
                        .participantScope(MatchParticipantScope.AT_LEAST_ONE)
                        .disambiguationScope(DisambiguationScope.ORIGIN_MATCHES)
                        .outcomeScope(MatchOutcomeScope.ALL_MATCHES)
                        .build())))
                .ambiguousPositions(new HashSet<>(
                        singletonList(zeroPos)))
                .build();
    }

    public List<Uid> determinedUids() {
        return positions.values().stream()
                .flatMap(p -> p.getCompetingUids().stream())
                .collect(toList());
    }

    public List<GroupPosition> ambiguousGroups() {
        return ambiguousPositions.stream()
                .map(idx -> positions.get(idx))
                .collect(toList());
    }
}
