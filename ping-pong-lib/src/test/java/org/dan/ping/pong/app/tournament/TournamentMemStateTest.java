package org.dan.ping.pong.app.tournament;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.dan.ping.pong.app.group.GroupServiceTest.UID2;
import static org.dan.ping.pong.app.match.rule.GroupParticipantOrderServiceTest.UID3;
import static org.dan.ping.pong.app.match.rule.GroupParticipantOrderServiceTest.UID4;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.dan.ping.pong.app.bid.Uid;
import org.junit.Test;

import java.util.Map;
import java.util.Optional;

public class TournamentMemStateTest {
    private final int CID = 3;
    private final int gid1 = 1;
    private final int gid2 = 2;

    private Map<Uid, ParticipantMemState> participants =
            ImmutableMap.<Uid, ParticipantMemState>builder()
                    .put(UID2, ParticipantMemState
                            .builder()
                            .uid(UID2)
                            .cid(CID)
                            .gid(Optional.of(gid1))
                            .build())
                    .put(UID3, ParticipantMemState
                            .builder()
                            .uid(UID3)
                            .cid(CID)
                            .gid(Optional.of(gid2))
                            .build())
                    .put(UID4, ParticipantMemState
                            .builder()
                            .uid(UID4)
                            .cid(CID)
                            .gid(Optional.empty())
                            .build())
                    .build();

    private TournamentMemState tournament = TournamentMemState
            .builder()
            .participants(participants)
            .build();

    @Test
    public void uidsInGroup() {
        assertThat(tournament.uidsInGroup(gid1), is(singleton(UID2)));
    }

    @Test
    public void uidsInCategory() {
        assertThat(tournament.uidsInCategory(CID - 2), is(emptySet()));
        assertThat(tournament.uidsInCategory(CID), is(ImmutableSet.of(UID3, UID4, UID2)));
    }
}
