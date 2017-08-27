package org.dan.ping.pong.app.tournament;

import static org.dan.ping.pong.app.tournament.TournamentResource.TOURNAMENT_COPY;
import static org.dan.ping.pong.app.tournament.TournamentState.Draft;
import static org.dan.ping.pong.mock.Generators.genStr;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.dan.ping.pong.mock.simulator.SimulatorParams.T_1_Q_1_G_8;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.mock.simulator.Simulator;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.dan.ping.pong.util.time.Clocker;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import java.time.Instant;
import java.util.Optional;

import javax.inject.Inject;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class TournamentCopyJerseyTest extends AbstractSpringJerseyTest {
    @Inject
    private Simulator simulator;

    @Inject
    private TournamentService tournamentService;

    @Inject
    private Clocker clocker;

    @Test
    public void tournamentResult() {
        final TournamentScenario scenario = TournamentScenario.begin()
                .name("copyCompleteTour")
                .category(c1, p1, p2)
                .w31(p1, p2)
                .quitsGroup(p1, p2)
                .champions(c1, p1);

        simulator.simulate(T_1_Q_1_G_8, scenario);

        final String newName = genStr();
        final Instant opensAt = clocker.get();
        final int copyTid = myRest().post(TOURNAMENT_COPY, scenario.getTestAdmin(),
                CopyTournament.builder()
                        .name(newName)
                        .opensAt(opensAt)
                        .originTid(scenario.getTid())
                        .build())
                .readEntity(Integer.class);

        assertThat(copyTid, Matchers.greaterThan(scenario.getTid()));

        final DraftingTournamentInfo tinfo = tournamentService
                .getDraftingTournament(copyTid,
                        Optional.of(scenario.getTestAdmin().getUid()));
        assertThat(tinfo.getPreviousTid(), is(Optional.of(scenario.getTid())));
        assertThat(tinfo.getOpensAt(), is(opensAt));
        assertThat(tinfo.getName(), is(newName));
        assertThat(tinfo.isIAmAdmin(), is(true));
        assertThat(tinfo.getPlace().getPid(), is(scenario.getPlaceId()));
        assertThat(tinfo.getState(), is(Draft));
        scenario.getCategoryDbId().keySet().forEach(cid -> assertThat(
                tinfo.getCategories(), hasItem(
                        hasProperty("name", containsString(cid.toString())))));
    }
}
