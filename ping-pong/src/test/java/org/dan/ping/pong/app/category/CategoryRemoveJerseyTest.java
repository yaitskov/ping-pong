package org.dan.ping.pong.app.category;

import static org.dan.ping.pong.app.category.CategoryResource.CATEGORY_DELETE;
import static org.dan.ping.pong.app.tournament.TournamentRulesConst.RULES_G2Q1_S3A2G11;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.dan.ping.pong.mock.simulator.TournamentScenario.begin;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.tournament.JerseyWithSimulator;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.dan.ping.pong.mock.simulator.imerative.ImperativeSimulator;
import org.dan.ping.pong.mock.simulator.imerative.ImperativeSimulatorFactory;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class CategoryRemoveJerseyTest extends AbstractSpringJerseyTest {
    @Inject
    private ImperativeSimulatorFactory isf;

    @Test
    public void remove() {
        final TournamentScenario scenario = begin().name("removeCategory")
                .rules(RULES_G2Q1_S3A2G11)
                .category(c1, p1, p2);
        final ImperativeSimulator simulator = isf.create(scenario);
        simulator.run(ImperativeSimulator::beginTournament);

        assertThat(myRest().post(
                CATEGORY_DELETE,
                scenario,
                RemoveCategory.builder()
                        .cid(scenario.getCategoryDbId().get(c1))
                        .tid(scenario.getTid())
                        .build()).getStatus(),
                is(200));
    }
}
