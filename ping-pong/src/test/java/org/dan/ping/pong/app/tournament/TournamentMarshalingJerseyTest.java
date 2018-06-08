package org.dan.ping.pong.app.tournament;

import static org.dan.ping.pong.app.tournament.TournamentRulesConst.RULES_G8Q2_S1A2G11;
import static org.dan.ping.pong.app.tournament.marshaling.TournamentMarshalingResource.TOURNAMENT_EXPORT_STATE;
import static org.dan.ping.pong.app.tournament.marshaling.TournamentMarshalingResource.TOURNAMENT_IMPORT_STATE;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.dan.ping.pong.mock.simulator.TournamentScenario.begin;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.junit.MatcherAssert.assertThat;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.tournament.marshaling.ImportTournamentState;
import org.dan.ping.pong.app.tournament.marshaling.TournamentEnvelope;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.dan.ping.pong.mock.simulator.imerative.ImperativeSimulatorFactory;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.dan.ping.pong.util.time.Clocker;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class TournamentMarshalingJerseyTest extends AbstractSpringJerseyTest {
    @Inject
    private ImperativeSimulatorFactory isf;

    @Inject
    private Clocker clocker;

    @Test
    public void marshalTournamentOf3InTheMiddle() {
        final TournamentScenario scenario = begin()
                .name("marshalTournamentOf3InMid")
                .rules(RULES_G8Q2_S1A2G11)
                .category(c1, p1, p2, p3);

        isf.create(scenario)
                .run(c -> c.beginTournament()
                        .scoreSet(p1, 11, p3, 0)
                        .scoreSet(p1, 11, p2, 0)
                        .scoreSet(p3, 11, p2, 0));

        final TournamentEnvelope expTournament = myRest()
                .get(TOURNAMENT_EXPORT_STATE + scenario.getTid(),
                        TournamentEnvelope.class);

        assertThat(expTournament.getExportedAt(),
                lessThanOrEqualTo(clocker.get()));

        final Tid exportedTid = myRest()
                .post(
                        TOURNAMENT_IMPORT_STATE,
                        scenario.getTestAdmin(),
                        ImportTournamentState
                                .builder()
                                .placeId(scenario.getPlaceId())
                                .tournament(expTournament)
                                .build())
                .readEntity(Tid.class);

        assertThat(scenario.getTid(), lessThan(exportedTid));
    }
}
