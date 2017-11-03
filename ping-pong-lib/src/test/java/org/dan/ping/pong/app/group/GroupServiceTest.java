package org.dan.ping.pong.app.group;

import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import org.dan.ping.pong.app.tournament.OpenTournamentMemState;
import org.dan.ping.pong.app.tournament.ParticipantMemState;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.bid.Uid;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Optional;

public class GroupServiceTest {
    private static final int CID = 1;
    private static final int GID = 1;
    private static final Uid UID1 = new Uid(1);
    GroupService sut;

    @Before
    public void setUp() {
        sut = new GroupService();
    }

    @Test
    public void zeroPopulationsNoGroups() {
        assertThat(sut.populations(OpenTournamentMemState.builder()
                        .participants(ImmutableMap.of(UID1,
                                ParticipantMemState.builder()
                                        .gid(Optional.empty())
                                        .uid(UID1)
                                        .cid(CID)
                                        .tid(new Tid(1))
                                        .build()))
                        .groups(Collections.emptyMap())
                        .build(), CID),
                allOf(hasProperty("links", hasSize(0)),
                        hasProperty("populations", hasSize(0))));
    }

    @Test
    public void populations1Person() {
        assertThat(sut.populations(OpenTournamentMemState.builder()
                        .participants(ImmutableMap.of(UID1, ParticipantMemState.builder()
                                .gid(Optional.of(GID))
                                .uid(UID1)
                                .cid(CID)
                                .tid(new Tid(1))
                                .build()))
                        .groups(ImmutableMap.of(GID, GroupInfo.builder().cid(CID).gid(GID).build()))
                        .build(), CID),
                allOf(hasProperty("links", hasSize(1)),
                        hasProperty("populations", is(singletonList(1L)))));
    }
}
