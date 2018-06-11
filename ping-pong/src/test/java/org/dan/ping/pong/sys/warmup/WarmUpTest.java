package org.dan.ping.pong.sys.warmup;


import static org.dan.ping.pong.app.tournament.Kw04FirstTournamentJerseyTest.RULES_G8Q1_S1A2G11_NP;
import static org.dan.ping.pong.mock.simulator.TournamentScenario.begin;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.tournament.JerseyWithSimulator;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.dan.ping.pong.mock.simulator.imerative.ImperativeSimulator;
import org.dan.ping.pong.mock.simulator.imerative.ImperativeSimulatorFactory;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.dan.ping.pong.util.time.Clocker;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class WarmUpTest extends AbstractSpringJerseyTest {
    @Inject
    private ImperativeSimulatorFactory isf;

    @Inject
    private WarmUpService warmUpService;

    @Inject
    private Clocker clocker;

    @Test
    public void warm() {
        final TournamentScenario scenario = begin().name("warmup")
                .rules(RULES_G8Q1_S1A2G11_NP);
        isf.create(scenario)
                .run(ImperativeSimulator::beginTournament);
        final int wmId = myRest()
                .post(WarmUpResource.WARM_UP, scenario.getTestAdmin(),
                        WarmUpRequest
                                .builder()
                                .action("hello")
                                .clientTime(clocker.get())
                                .build())
                .readEntity(Integer.class);
        assertThat(wmId, greaterThan(0));
        assertThat(warmUpService.logDuration(wmId), is(1));
    }

    @Inject
    private WarmUpDao warmUpDao;

    @Test
    public void cleanUp() {
        warm();
        assertThat(warmUpDao.cleanOlderThan(clocker.get().plusSeconds(1000)),
                greaterThan(0));
    }
}
