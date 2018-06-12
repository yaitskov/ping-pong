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
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class WarmUpTest extends AbstractSpringJerseyTest {
    @Inject
    private ImperativeSimulatorFactory isf;

    @Inject
    private WarmUpDao warmUpDao;

    @Inject
    private Clocker clocker;

    @Test
    public void warm() {
        warm("hello");
    }

    private void warm(String action) {
        final TournamentScenario scenario = begin().name("warmUp " + action)
                .rules(RULES_G8Q1_S1A2G11_NP);
        isf.create(scenario)
                .run(ImperativeSimulator::beginTournament);
        final int wmId = doWarmUp(scenario, null, action);
        assertThat(wmId, greaterThan(0));
        assertThat(doWarmUp(scenario, null, action), is(0));
        assertThat("filter is not triggered", warmUpDao.readDuration(wmId), is(0L));
        assertThat(doWarmUp(scenario, wmId, action), is(0));
        assertThat("filter is triggered", warmUpDao.readDuration(wmId), greaterThan(0L));
    }

    private Integer doWarmUp(TournamentScenario scenario, Integer wmId, String action) {
        final Invocation.Builder builder = myRest()
                .postBuilder(WarmUpResource.WARM_UP, scenario.getTestAdmin().getSession());
        if (wmId != null) {
            builder.header(WarmUpHttpFilter.CS_WARM_UP_ID, wmId);
        }
        return builder
                .post(Entity.entity(WarmUpRequest
                                .builder()
                                .action(action)
                                .clientTime(clocker.get().minusMillis(1))
                                .build(),
                        MediaType.APPLICATION_JSON_TYPE))
                .readEntity(Integer.class);
    }

    @Test
    public void cleanUp() {
        warm("cleanUp");
        assertThat(warmUpDao.cleanOlderThan(clocker.get().plusSeconds(1000)),
                greaterThan(0));
    }
}
