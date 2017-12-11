package org.dan.ping.pong.app.match;

import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_G8Q1_S1A2G11;
import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_G8Q1_S3A2G11;
import static org.dan.ping.pong.app.match.MatchResource.MATCH_LIST_JUDGED;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.dan.ping.pong.mock.simulator.TournamentScenario.begin;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertThat;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.tournament.JerseyWithSimulator;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.dan.ping.pong.mock.simulator.imerative.ImperativeSimulatorFactory;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;

import javax.inject.Inject;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class ListJudgedMatchesJerseyTest extends AbstractSpringJerseyTest {
    @Inject
    private ImperativeSimulatorFactory isf;

    @Test
    public void withExpelled() {
        final TournamentScenario scenario = begin().name("groupOf3")
                .rules(RULES_G8Q1_S3A2G11)
                .category(c1, p1, p2, p3);
        isf.create(scenario)
                .run(c -> c.beginTournament()
                        .scoreSet3(p1, 11, p3, 2)
                        .expelPlayer(p1)
                        .scoreSet(p2, 11, p3, 1));

        final int tid = scenario.getTid().getTid();

        final Uid uidP1 = scenario.player2Uid(p1);
        final Uid uidP2 = scenario.player2Uid(p2);

        assertThat(myRest().get(MATCH_LIST_JUDGED + tid + "/"
                        + uidP1.getId(), PlayedMatchList.class),
                allOf(
                        hasProperty("progress",
                                allOf(
                                        hasProperty("totalMatches", is(2L)),
                                        hasProperty("leftMatches", is(0L)))),
                        hasProperty("participant",
                                hasProperty("uid", is(uidP1))),
                        hasProperty("inGroup", hasItem(allOf(
                                hasProperty("opponent", hasProperty("uid", is(uidP2))),
                                hasProperty("winnerUid", is(Optional.of(uidP2))))))));

        final Uid uidP3 = scenario.player2Uid(p3);

        assertThat(myRest().get(MATCH_LIST_JUDGED + tid + "/"
                        + uidP2.getId(), PlayedMatchList.class),
                allOf(
                        hasProperty("progress",
                                allOf(
                                        hasProperty("totalMatches", is(2L)),
                                        hasProperty("leftMatches", is(1L)))),
                        hasProperty("participant",
                                hasProperty("uid", is(uidP2))),
                        hasProperty("inGroup", hasItems(
                                allOf(
                                        hasProperty("opponent", hasProperty("uid", is(uidP1))),
                                        hasProperty("winnerUid", is(Optional.of(uidP2)))),
                                allOf(
                                        hasProperty("opponent", hasProperty("uid", is(uidP3))),
                                        hasProperty("winnerUid", is(Optional.empty())))))));
    }
}
