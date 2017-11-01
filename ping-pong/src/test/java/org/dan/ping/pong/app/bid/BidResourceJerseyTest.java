package org.dan.ping.pong.app.bid;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;
import static org.dan.ping.pong.app.bid.BidResource.FIND_BIDS_BY_STATE;
import static org.dan.ping.pong.app.bid.BidState.Play;
import static org.dan.ping.pong.app.bid.BidState.Wait;
import static org.dan.ping.pong.app.match.MatchJerseyTest.RULES_G8Q1_S3A2G11;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.tournament.JerseyWithSimulator;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.user.UserLink;
import org.dan.ping.pong.mock.simulator.Simulator;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.core.GenericType;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class BidResourceJerseyTest extends AbstractSpringJerseyTest {
    @Inject
    private Simulator simulator;

    @Test
    public void findByState() {
        final TournamentScenario scenario = TournamentScenario.begin()
                .ignoreUnexpectedGames()
                .name("findByState")
                .rules(RULES_G8Q1_S3A2G11)
                .category(c1, p1, p2);

        simulator.simulate(scenario);

        final List<UserLink> result = myRest().post(
                FIND_BIDS_BY_STATE,
                scenario.getTestAdmin(),
                FindByState.builder().tid(scenario.getTid())
                        .states(asList(Wait, Play)).build())
                .readEntity(
                        new GenericType<List<UserLink>>() {});

        assertThat(result.stream().map(UserLink::getUid)
                .collect(toSet()), is(scenario.getUidPlayer().keySet()));
    }
}
