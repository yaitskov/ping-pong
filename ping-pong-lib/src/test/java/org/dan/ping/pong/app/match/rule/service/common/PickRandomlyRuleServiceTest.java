package org.dan.ping.pong.app.match.rule.service.common;

import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.dan.ping.pong.app.match.rule.GroupParticipantOrderServiceTest.BID3;
import static org.dan.ping.pong.app.match.rule.GroupParticipantOrderServiceTest.BID4;
import static org.dan.ping.pong.app.match.rule.GroupParticipantOrderServiceTest.BID5;
import static org.dan.ping.pong.app.match.rule.GroupParticipantOrderServiceTest.BID6;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.match.MatchInfo;
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
    public static final Bid BID7 = new Bid(7);
    public static final Uid UID8 = new Uid(8);
    public static final Bid BID8 = new Bid(8);

    public static final Supplier<Stream<MatchInfo>> FAILING_SUPPLIER = () -> {
        throw new IllegalStateException();
    };

    private PickRandomlyRuleService sut = new PickRandomlyRuleService();

    @Test
    public void returnIfDisambuationMatchesCreated() {
        final GroupRuleParams params = GroupRuleParams.ofParams(1, null, null, null, singleton(null));
        params.setDisambiguationMatchesWillBeCreated(true);
        assertThat(sut.score(FAILING_SUPPLIER, null,
                new PickRandomlyRule(), params),
                is(Optional.empty()));
    }

    @Test
    public void shuffleOneGroup() {
        final Set<Bid> uidsProvider = ImmutableSet.of(BID3, BID4, BID5);
        List<Bid> uids = shuffle(uidsProvider, 3);
        assertThat(uids, is(shuffle(uidsProvider, 3)));
        assertThat(uids, not(is(shuffle(uidsProvider, 2))));
    }

    @Test
    public void keepOrderWithInGroupWhenShuffleManyGroups() {
        final Set<Bid> uidsProvider = ImmutableSet.of(BID3, BID4, BID5, BID6, BID7, BID8);
        final TournamentMemState tournament = tournamentWithParticipants(uidsProvider);
        final List<Bid> g1Uids = shuffleOneGroup(tournament, 1);
        final List<Bid> g2Uids = shuffleOneGroup(tournament, 2);
        final List<Bid> bids = shuffle(uidsProvider, 0, tournament);
        assertThat(bids, is(shuffle(uidsProvider, 0, tournament)));

        validateOrder(g1Uids, bids);
        validateOrder(g2Uids, bids);
    }

    @Test
    public void mixUidsFromDifferentGroups() {
        final Set<Bid> uidsProvider = ImmutableSet.of(BID3, BID4, BID5, BID6, BID7, BID8);
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

    private TournamentMemState tournamentWithParticipants(Set<Bid> bidsProvider) {
        return TournamentMemState.builder()
                .participants(
                        bidsProvider
                                .stream()
                                .collect(toMap(
                                        o -> o,
                                        o -> ParticipantMemState
                                                .builder()
                                                .bid(o)
                                                .uid(new Uid(o.intValue()))
                                                .gid(Optional.of(o.intValue() % 2 == 0 ? 1 : 2))
                                                .build())))
                .build();
    }

    private void validateOrder(List<Bid> g1Bids, List<Bid> bids) {
        for (int i = 0; i < g1Bids.size(); ++i) {
            final Bid uid = g1Bids.get(i);
            final int uidIdx = bids.indexOf(uid);
            for (int j = i + 1; j < g1Bids.size(); ++j) {
                assertThat(uidIdx, lessThan(bids.indexOf(g1Bids.get(j))));
            }
        }
    }

    private List<Bid> shuffleOneGroup(TournamentMemState tournament, int gid) {
        return shuffle(
                tournament.getParticipants()
                        .values()
                        .stream()
                        .filter(p -> p.getGid().get().equals(gid))
                        .map(ParticipantMemState::getBid)
                        .collect(toSet()),
                gid);
    }

    private List<Bid> shuffle(Set<Bid> bidsProvider, int gid) {
        return shuffle(bidsProvider, gid, null);
    }

    private List<Bid> shuffle(Set<Bid> bidsProvider, int gid,
            TournamentMemState tournament) {
        return sut.score(FAILING_SUPPLIER, bidsProvider,
                new PickRandomlyRule(),
                GroupRuleParams.ofParams(gid, tournament, null, null, bidsProvider))
                .get()
                .map(Reason::getBid)
                .collect(toList());
    }
}


