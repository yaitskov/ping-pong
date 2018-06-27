package org.dan.ping.pong.app.match;

import static org.dan.ping.pong.app.match.MatchResource.MATCH_FIND_BY_PARTICIPANTS;
import static org.dan.ping.pong.app.match.MatchResource.MATCH_LIST_JUDGED;
import static org.dan.ping.pong.app.match.MatchState.Over;
import static org.dan.ping.pong.app.match.MatchType.POff;
import static org.dan.ping.pong.app.tournament.ParticipantMemState.FILLER_LOSER_BID;
import static org.dan.ping.pong.app.tournament.TournamentRulesConst.RULES_G8Q1_S3A2G11;
import static org.dan.ping.pong.app.tournament.TournamentRulesConst.RULES_JP_S1A2G11;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.dan.ping.pong.mock.simulator.TournamentScenario.begin;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertThat;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.tournament.JerseyWithSimulator;
import org.dan.ping.pong.app.user.UserRole;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.dan.ping.pong.mock.simulator.imerative.ImperativeSimulator;
import org.dan.ping.pong.mock.simulator.imerative.ImperativeSimulatorFactory;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.core.GenericType;

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

        final Bid bidP1 = scenario.player2Bid(p1);
        final Bid bidP2 = scenario.player2Bid(p2);

        assertThat(myRest().get(MATCH_LIST_JUDGED + tid + "/"
                        + bidP1.intValue(), PlayedMatchList.class),
                allOf(
                        hasProperty("progress",
                                allOf(
                                        hasProperty("totalMatches", is(2L)),
                                        hasProperty("leftMatches", is(0L)))),
                        hasProperty("participant",
                                hasProperty("uid", is(bidP1))),
                        hasProperty("inGroup", hasItem(allOf(
                                hasProperty("opponent", hasProperty("uid", is(bidP2))),
                                hasProperty("winnerUid", is(Optional.of(bidP2))))))));

        final Bid bidP3 = scenario.player2Bid(p3);

        assertThat(myRest().get(MATCH_LIST_JUDGED + tid + "/"
                        + bidP2.intValue(), PlayedMatchList.class),
                allOf(
                        hasProperty("progress",
                                allOf(
                                        hasProperty("totalMatches", is(2L)),
                                        hasProperty("leftMatches", is(1L)))),
                        hasProperty("participant",
                                hasProperty("uid", is(bidP2))),
                        hasProperty("inGroup", hasItems(
                                allOf(
                                        hasProperty("opponent", hasProperty("uid", is(bidP1))),
                                        hasProperty("winnerUid", is(Optional.of(bidP2)))),
                                allOf(
                                        hasProperty("opponent", hasProperty("uid", is(bidP3))),
                                        hasProperty("winnerUid", is(Optional.empty())))))));
    }

    @Test
    public void playOffWithBye() {
        final TournamentScenario scenario = begin().name("playOffWithBye")
                .rules(RULES_JP_S1A2G11)
                .category(c1, p1, p2, p3);
        final ImperativeSimulator simulator = isf.create(scenario);
        simulator.run(ImperativeSimulator::beginTournament);

        final int tid = scenario.getTid().getTid();
        final Bid bidP1 = scenario.player2Bid(p1);

        final Mid mid = myRest().get(MATCH_FIND_BY_PARTICIPANTS + tid + "/" + bidP1.intValue() + "/1",
                new GenericType<List<Mid>>(){}).get(0);

        assertThat(myRest().get(MATCH_LIST_JUDGED + tid + "/"
                        + bidP1.intValue(), PlayedMatchList.class),
                allOf(
                        hasProperty("participant", hasProperty("uid", is(bidP1))),
                        hasProperty("inGroup", is(Collections.emptyList())),
                        hasProperty("playOff", everyItem(
                                allOf(
                                        hasProperty("winnerUid", is(Optional.of(bidP1))),
                                        hasProperty("opponent", hasProperty("uid", is(FILLER_LOSER_BID))),
                                        hasProperty("mid", is(mid)))))));

        simulator.run(c -> assertThat(c.matchResult(mid),
                allOf(
                        hasProperty("role", is(UserRole.Spectator)),
                        hasProperty("type", is(POff)),
                        hasProperty("state", is(Over)),
                        hasProperty("playedSets", is(0)),
                        hasProperty("disputes", is(0)),
                        hasProperty("participants", hasItems(
                                hasProperty("uid", is(FILLER_LOSER_BID)),
                                hasProperty("uid", is(bidP1)))))));
    }
}
