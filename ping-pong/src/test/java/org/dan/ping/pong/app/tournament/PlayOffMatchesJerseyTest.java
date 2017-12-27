package org.dan.ping.pong.app.tournament;

import static java.util.Collections.emptyList;
import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_JP_S1A2G11;
import static org.dan.ping.pong.app.match.MatchState.Draft;
import static org.dan.ping.pong.app.match.MatchState.Game;
import static org.dan.ping.pong.app.match.MatchState.Over;
import static org.dan.ping.pong.app.tournament.ParticipantMemState.FILLER_LOSER_UID;
import static org.dan.ping.pong.app.tournament.TournamentResource.TOURNAMENT_PLAY_OFF_MATCHES;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.Player.p4;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.match.Mid;
import org.dan.ping.pong.app.playoff.PlayOffMatches;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.dan.ping.pong.mock.simulator.imerative.ImperativeSimulator;
import org.dan.ping.pong.mock.simulator.imerative.ImperativeSimulatorFactory;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;

import javax.inject.Inject;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class PlayOffMatchesJerseyTest extends AbstractSpringJerseyTest {
    @Inject
    private ImperativeSimulatorFactory isf;

    @Test
    public void playOff1Defeat2Guys() {
        final TournamentScenario scenario = TournamentScenario.begin().name("playOff1Defeat2Guys")
                .rules(RULES_JP_S1A2G11)
                .category(c1, p1, p2);
        final ImperativeSimulator simulator = isf.create(scenario);
        simulator.run(ImperativeSimulator::beginTournament);

        final int cid = scenario.getCategoryDbId().get(c1);
        final PlayOffMatches matches = myRest().get(
                TOURNAMENT_PLAY_OFF_MATCHES + scenario.getTid().getTid() + "/" + cid,
                PlayOffMatches.class);

        assertThat(matches.getMatches().stream().findAny().get(),
                allOf(
                        hasProperty("level", is(1)),
                        hasProperty("id", is(simulator.resolveMid(p1, p2))),
                        hasProperty("state", is(Game)),
                        hasProperty("score", is(ImmutableMap.of(scenario.player2Uid(p1), 0,
                                scenario.player2Uid(p2), 0))),
                        hasProperty("winnerId", is(Optional.empty())),
                        hasProperty("walkOver", is(false))));
        assertEquals(emptyList(), matches.getTransitions());
        assertEquals(scenario.getUidPlayer().keySet(), matches.getParticipants().keySet());
    }

    @Test
    public void playOff1Defeat4Guys() {
        final TournamentScenario scenario = TournamentScenario.begin().name("playOff1Defeat4Guys")
                .tables(2)
                .rules(RULES_JP_S1A2G11)
                .category(c1, p1, p2, p3, p4);
        final ImperativeSimulator simulator = isf.create(scenario);
        simulator.run(ImperativeSimulator::beginTournament);

        final Mid p1p4Match = simulator.resolveMid(p1, p4);
        final Mid p2p3Match = simulator.resolveMid(p2, p3);
        playOff1Defeat4Guys_when_no_MatchesPlayed(p1p4Match, p2p3Match, scenario, matches(scenario));
        simulator.run(c -> c.scoreSet(p1, 11, p4, 4));
        playOff1Defeat4Guys_when_p1p4Played(p1p4Match, p2p3Match, scenario, matches(scenario));
        simulator.run(c -> c.playerQuits(p3));
        playOff1Defeat4Guys_when_p3Quit(p1p4Match, p2p3Match, scenario, matches(scenario));
    }

    private PlayOffMatches matches(TournamentScenario scenario) {
        final int cid = scenario.getCategoryDbId().get(c1);
        return myRest().get(
                TOURNAMENT_PLAY_OFF_MATCHES + scenario.getTid().getTid() + "/" + cid,
                PlayOffMatches.class);
    }

    private void playOff1Defeat4Guys_when_no_MatchesPlayed(Mid p1p4Match, Mid p2p3Match,
            TournamentScenario scenario, PlayOffMatches matches) {
        assertThat(matches.getMatches().stream()
                        .filter(m -> m.getId().equals(p1p4Match)).findAny().get(),
                allOf(
                        hasProperty("level", is(1)),
                        hasProperty("id", is(p1p4Match)),
                        hasProperty("state", is(Game)),
                        hasProperty("score", is(ImmutableMap.of(scenario.player2Uid(p1), 0,
                                scenario.player2Uid(p4), 0))),
                        hasProperty("winnerId", is(Optional.empty())),
                        hasProperty("walkOver", is(false))));

        assertThat(matches.getMatches().stream()
                        .filter(m -> m.getId().equals(p2p3Match)).findAny().get(),
                allOf(
                        hasProperty("level", is(1)),
                        hasProperty("id", is(p2p3Match)),
                        hasProperty("state", is(Game)),
                        hasProperty("score", is(ImmutableMap.of(scenario.player2Uid(p2), 0,
                                scenario.player2Uid(p3), 0))),
                        hasProperty("winnerId", is(Optional.empty())),
                        hasProperty("walkOver", is(false))));

        assertThat(matches.getMatches().stream()
                        .filter(m -> !m.getId().equals(p2p3Match)
                                && !m.getId().equals(p1p4Match))
                        .findAny().get(),
                allOf(
                        hasProperty("level", is(2)),
                        hasProperty("id", notNullValue()),
                        hasProperty("state", is(Draft)),
                        hasProperty("winnerId", is(Optional.empty())),
                        hasProperty("walkOver", is(false))));

        assertThat(matches.getTransitions(),
                hasItems(
                        hasProperty("from", is(p1p4Match)),
                        hasProperty("from", is(p2p3Match))));

        assertEquals(scenario.getUidPlayer().keySet(), matches.getParticipants().keySet());
    }

    private void playOff1Defeat4Guys_when_p1p4Played(Mid p1p4Match, Mid p2p3Match,
            TournamentScenario scenario, PlayOffMatches matches) {
        final Uid uid1 = scenario.player2Uid(p1);
        assertThat(matches.getMatches().stream()
                        .filter(m -> m.getId().equals(p1p4Match)).findAny().get(),
                allOf(
                        hasProperty("level", is(1)),
                        hasProperty("id", is(p1p4Match)),
                        hasProperty("state", is(Over)),
                        hasProperty("score", is(ImmutableMap.of(uid1, 1,
                                scenario.player2Uid(p4), 0))),
                        hasProperty("winnerId", is(Optional.of(uid1))),
                        hasProperty("walkOver", is(false))));

        assertThat(matches.getMatches().stream()
                        .filter(m -> !m.getId().equals(p2p3Match)
                                && !m.getId().equals(p1p4Match))
                        .findAny().get(),
                allOf(
                        hasProperty("level", is(2)),
                        hasProperty("id", notNullValue()),
                        hasProperty("state", is(Draft)),
                        hasProperty("score", is(ImmutableMap.of(scenario.player2Uid(p1), 0))),
                        hasProperty("winnerId", is(Optional.empty())),
                        hasProperty("walkOver", is(false))));

        assertEquals(scenario.getUidPlayer().keySet(), matches.getParticipants().keySet());
    }

    private void playOff1Defeat4Guys_when_p3Quit(Mid p1p4Match, Mid p2p3Match,
            TournamentScenario scenario, PlayOffMatches matches) {
        final Uid uid2 = scenario.player2Uid(p2);
        assertThat(matches.getMatches().stream()
                        .filter(m -> m.getId().equals(p2p3Match)).findAny().get(),
                allOf(
                        hasProperty("level", is(1)),
                        hasProperty("id", is(p2p3Match)),
                        hasProperty("state", is(Over)),
                        hasProperty("score", is(ImmutableMap.of(uid2, 0,
                                scenario.player2Uid(p3), 0))),
                        hasProperty("winnerId", is(Optional.of(uid2))),
                        hasProperty("walkOver", is(true))));

        assertThat(matches.getMatches().stream()
                        .filter(m -> !m.getId().equals(p2p3Match)
                                && !m.getId().equals(p1p4Match))
                        .findAny().get(),
                allOf(
                        hasProperty("level", is(2)),
                        hasProperty("id", notNullValue()),
                        hasProperty("score", is(ImmutableMap.of(uid2, 0,
                                scenario.player2Uid(p1), 0))),
                        hasProperty("state", is(Game)),
                        hasProperty("winnerId", is(Optional.empty())),
                        hasProperty("walkOver", is(false))));
    }

    @Test
    public void justPlayOff3() {
        final TournamentScenario scenario = TournamentScenario.begin()
                .name("justPlayOff3")
                .rules(RULES_JP_S1A2G11)
                .category(c1, p1, p2, p3);
        final ImperativeSimulator simulator = isf.create(scenario);
        simulator.run(ImperativeSimulator::beginTournament);

        final PlayOffMatches matches = matches(scenario);

        final Uid uid1 = scenario.player2Uid(p1);
        final Uid uid3 = scenario.player2Uid(p3);
        final Uid uid2 = scenario.player2Uid(p2);
        assertThat(matches.getMatches(),
                hasItems(
                        allOf(
                                hasProperty("level", is(1)),
                                hasProperty("state", is(Over)),
                                hasProperty("walkOver", is(true)),
                                hasProperty("score", is(ImmutableMap.of(
                                        FILLER_LOSER_UID, 0,
                                        uid1, 0))),
                                hasProperty("winnerId", is(Optional.of(uid1)))),
                        allOf(
                                hasProperty("level", is(1)),
                                hasProperty("state", is(Game)),
                                hasProperty("score", is(ImmutableMap.of(
                                        uid3, 0,
                                        uid2, 0))),
                                hasProperty("walkOver", is(false)),
                                hasProperty("winnerId", is(Optional.empty())))));

        assertThat(matches.getParticipants().keySet(),
                allOf(
                        not(hasItem(FILLER_LOSER_UID)),
                        hasItems(uid1, uid2, uid3)));
    }
}
