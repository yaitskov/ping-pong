package org.dan.ping.pong.app.group;

import static org.dan.ping.pong.app.group.GroupResource.GROUP_LIST;
import static org.dan.ping.pong.app.group.GroupResource.GROUP_RESULT;
import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_G8Q1_S1A2G11;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.dan.ping.pong.mock.simulator.TournamentScenario.begin;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertThat;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.tournament.JerseyWithSimulator;
import org.dan.ping.pong.mock.simulator.Player;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.dan.ping.pong.mock.simulator.imerative.ImperativeSimulatorFactory;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class GroupResultJerseyTest extends AbstractSpringJerseyTest {
    @Inject
    private ImperativeSimulatorFactory isf;

    @Test
    public void groupOf3() {
        final TournamentScenario scenario = begin().name("groupOf3")
                .rules(RULES_G8Q1_S1A2G11)
                .category(c1, p1, p2, p3);
        isf.create(scenario)
                .run(c -> c.beginTournament()
                        .scoreSet(p1, 11, p3, 0)
                        .scoreSet(p1, 11, p2, 1)
                        .scoreSet(p2, 2, p3, 11));

        final int tid = scenario.getTid().getTid();

        final TournamentGroups g = myRest().get(GROUP_LIST + tid, TournamentGroups.class);
        final int gid = g.getGroups().stream().findFirst().get().getGid();
        final GroupParticipants r = myRest().get(GROUP_RESULT + tid + "/"
                + gid, GroupParticipants.class);

        assertThat(player(scenario, r, p1),
                allOf(
                        hasProperty("punkts", is(4)),
                        hasProperty("name", containsString("p1")),
                        hasProperty("seedPosition", is(0)),
                        hasProperty("finishPosition", is(0))));

        assertThat(player(scenario, r, p3),
                allOf(
                        hasProperty("punkts", is(3)),
                        hasProperty("name", containsString("p3")),
                        hasProperty("seedPosition", is(2)),
                        hasProperty("finishPosition", is(1))));
    }

    private GroupParticipantResult player(TournamentScenario scenario,
            GroupParticipants r, Player player) {
        return r.getParticipants().stream()
                .filter(
                        p -> p.getUid().equals(scenario.player2Uid(player)))
                .findAny()
                .get();
    }
}
