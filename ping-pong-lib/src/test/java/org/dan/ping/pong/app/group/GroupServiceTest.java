package org.dan.ping.pong.app.group;

import static java.util.Collections.singletonList;
import static org.dan.ping.pong.app.sport.SportType.PingPong;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.sport.Sports;
import org.dan.ping.pong.app.sport.pingpong.PingPongSport;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.TournamentMemState;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;


public class GroupServiceTest {
    private static final int CID = 1;
    public static final int GID = 1;
    public static final Uid UID2 = new Uid(2);

    GroupService sut;

    @Before
    public void setUp() {
        sut = GroupService
                .builder()
                .sports(new Sports(ImmutableMap.of(PingPong, new PingPongSport())))
                .build();
    }

    @Test
    public void zeroPopulationsNoGroups() {
        assertThat(sut.populations(tournamentForPopulations(
                Optional.empty(), Collections.emptyMap()), CID),
                allOf(hasProperty("links", hasSize(0)),
                        hasProperty("populations", hasSize(0))));
    }

    @Test
    public void populations1Person() {
        assertThat(sut.populations(tournamentForPopulations(Optional.of(GID),
                ImmutableMap.of(GID, GroupInfo.builder().cid(CID).gid(GID).build())), CID),
                allOf(hasProperty("links", hasSize(1)),
                        hasProperty("populations", is(singletonList(1L)))));
    }

    private TournamentMemState tournamentForPopulations(
            Optional<Integer> gid, Map<Integer, GroupInfo> groups) {
        return TournamentMemState.builder()
                        .participants(ImmutableMap
                                .of(UID2, ParticipantMemState.builder()
                                .gid(gid)
                                .uid(UID2)
                                .cid(CID)
                                .tid(new Tid(1))
                                .build()))
                        .groups(groups).build();
    }
}
