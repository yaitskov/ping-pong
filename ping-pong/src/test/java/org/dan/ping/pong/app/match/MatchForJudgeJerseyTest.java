package org.dan.ping.pong.app.match;

import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_JP_S1A2G11;
import static org.dan.ping.pong.app.match.MatchResource.MATCH_FIND_BY_PARTICIPANTS;
import static org.dan.ping.pong.app.match.MatchResource.MATCH_FOR_JUDGE;
import static org.dan.ping.pong.app.match.MatchType.POff;
import static org.dan.ping.pong.app.tournament.ParticipantMemState.FILLER_LOSER_UID;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.junit.MatcherAssert.assertThat;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.tournament.JerseyWithSimulator;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.dan.ping.pong.mock.simulator.imerative.ImperativeSimulator;
import org.dan.ping.pong.mock.simulator.imerative.ImperativeSimulatorFactory;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.core.GenericType;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class MatchForJudgeJerseyTest extends AbstractSpringJerseyTest {
    @Inject
    private ImperativeSimulatorFactory isf;

    @Test
    public void matchWithBye() {
        final TournamentScenario scenario = TournamentScenario.begin()
                .name("justPlayOff3")
                .rules(RULES_JP_S1A2G11)
                .category(c1, p1, p2, p3);
        final ImperativeSimulator simulator = isf.create(scenario);
        simulator.run(ImperativeSimulator::beginTournament);

        final int tid = scenario.getTid().getTid();
        final Uid uid1 = scenario.player2Uid(p1);
        final Mid mid = myRest().get(MATCH_FIND_BY_PARTICIPANTS + tid + "/"
                        + uid1.getId() + "/" + FILLER_LOSER_UID.getId(),
                new GenericType<List<Mid>>(){}).get(0);
        OpenMatchForJudge match = myRest().get(
                MATCH_FOR_JUDGE + tid + "/" + mid.getId(),
                OpenMatchForJudge.class);

        assertThat(match, allOf(
                hasProperty("mid", is(mid)),
                hasProperty("playedSets", is(0)),
                hasProperty("minGamesToWin", is(11)),
                hasProperty("minAdvanceInGames", is(2)),
                hasProperty("started", notNullValue()),
                hasProperty("matchType", is(POff)),
                hasProperty("tid", is(scenario.getTid())),
                hasProperty("table", is(Optional.empty())),
                hasProperty("participants", hasItems(
                        hasProperty("uid", is(FILLER_LOSER_UID)),
                        hasProperty("uid", is(uid1))))
                ));
    }
}
