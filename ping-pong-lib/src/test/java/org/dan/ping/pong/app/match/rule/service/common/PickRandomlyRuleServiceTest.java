package org.dan.ping.pong.app.match.rule.service.common;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.dan.ping.pong.app.match.rule.GroupParticipantOrderServiceTest.UID3;
import static org.dan.ping.pong.app.match.rule.GroupParticipantOrderServiceTest.UID4;
import static org.dan.ping.pong.app.match.rule.GroupParticipantOrderServiceTest.UID5;
import static org.dan.ping.pong.app.match.rule.GroupParticipantOrderServiceTest.UID6;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.rule.UidsProvider;
import org.dan.ping.pong.app.match.rule.reason.Reason;
import org.dan.ping.pong.app.match.rule.rules.common.PickRandomlyRule;
import org.dan.ping.pong.app.match.rule.service.GroupRuleParams;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class PickRandomlyRuleServiceTest {
    public static final Uid UID7 = new Uid(7);
    public static final Uid UID8 = new Uid(8);

    public static final Supplier<Stream<MatchInfo>> FAILING_SUPPLIER = () -> {
        throw new IllegalStateException();
    };

    private PickRandomlyRuleService sut = new PickRandomlyRuleService();

    @Test
    public void returnIfDisambuationMatchesCreated() {
        final GroupRuleParams params = GroupRuleParams.ofParams(1, null, null, null);
        params.setDisambiguationMatchesWillBeCreated(true);
        assertThat(sut.score(FAILING_SUPPLIER, null,
                new PickRandomlyRule(), params),
                is(Optional.empty()));
    }

    @Test
    public void shuffleOneGroup() {
        final UidsProvider uidsProvider = uids(ImmutableSet.of(UID3, UID4, UID5));
        List<Uid> uids = shuffle(uidsProvider, 3);
        assertThat(uids, is(shuffle(uidsProvider, 3)));
        assertThat(uids, not(is(shuffle(uidsProvider, 2))));
    }

    @Test
    public void keepOrderWithInGroupWhenShuffleManyGroups() {
        final UidsProvider uidsProvider = uids(ImmutableSet.of(UID3, UID4, UID5, UID6, UID7, UID8));
        final TournamentMemState tournament = tournamentWithParticipants(uidsProvider);
        final List<Uid> g1Uids = shuffleOneGroup(tournament, 1);
        final List<Uid> g2Uids = shuffleOneGroup(tournament, 2);
        final List<Uid> uids = shuffle(uidsProvider, 0, tournament);
        assertThat(uids, is(shuffle(uidsProvider, 0, tournament)));

        validateOrder(g1Uids, uids);
        validateOrder(g2Uids, uids);
    }

    @Test
    public void mixUidsFromDifferentGroups() {
        final UidsProvider uidsProvider = uids(ImmutableSet.of(UID3, UID4, UID5, UID6, UID7, UID8));
        final TournamentMemState tournament = tournamentWithParticipants(uidsProvider);
        final Map<Integer, Integer> gidUids = shuffle(uidsProvider, 0, tournament)
                .stream()
                .limit(uidsProvider.size() / 2)
                .collect(
                        toMap(o -> tournament.getParticipant(o).getGid().get(),
                                o -> 1, (a, b) -> a + b));

        assertThat(gidUids.get(2), allOf(greaterThan(0), lessThan(3)));
        assertThat(gidUids.get(1), allOf(greaterThan(0), lessThan(3)));
    }

    private TournamentMemState tournamentWithParticipants(UidsProvider uidsProvider) {
        return TournamentMemState.builder()
                .participants(uidsProvider.uids().stream()
                        .collect(toMap(o -> o, o -> ParticipantMemState
                                .builder()
                                .uid(o)
                                .gid(Optional.of(o.getId() % 2 == 0 ? 1 : 2))
                                .build())))
                .build();
    }

    private void validateOrder(List<Uid> g1Uids, List<Uid> uids) {
        for (int i = 0; i < g1Uids.size(); ++i) {
            final Uid uid = g1Uids.get(i);
            final int uidIdx = uids.indexOf(uid);
            for (int j = i + 1; j < g1Uids.size(); ++j) {
                assertThat(uidIdx, lessThan(uids.indexOf(g1Uids.get(j))));
            }
        }
    }

    private List<Uid> shuffleOneGroup(TournamentMemState tournament, int gid) {
        return shuffle(uids(tournament.getParticipants().values()
                .stream().filter(p -> p.getGid().get().equals(gid))
                .map(ParticipantMemState::getUid)
                .collect(toSet())), gid);
    }

    private UidsProvider uids(Set<Uid> of) {
        return new UidsProvider(of, FAILING_SUPPLIER);
    }

    private List<Uid> shuffle(UidsProvider uidsProvider, int gid) {
        return shuffle(uidsProvider, gid, null);
    }

    private List<Uid> shuffle(UidsProvider uidsProvider, int gid,
            TournamentMemState tournament) {
        return sut.score(FAILING_SUPPLIER, uidsProvider,
                new PickRandomlyRule(), GroupRuleParams.ofParams(gid, tournament, null, null))
                .get()
                .map(Reason::getUid)
                .collect(toList());
    }
}


