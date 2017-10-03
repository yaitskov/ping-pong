package org.dan.ping.pong.app.server.castinglots;

import static com.google.common.primitives.Ints.asList;
import static org.dan.ping.pong.app.server.castinglots.CastingLotsResource.CID_IN;
import static org.dan.ping.pong.app.server.castinglots.CastingLotsResource.GET_MANUAL_BIDS_ORDER;
import static org.dan.ping.pong.app.server.castinglots.CastingLotsResource.ORDER_BIDS_MANUALLY;
import static org.dan.ping.pong.app.server.match.MatchJerseyTest.RULES_G2Q1_S1A2G11_PRNK;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.dan.ping.pong.mock.simulator.ProvidedRank.R1;
import static org.dan.ping.pong.mock.simulator.ProvidedRank.R2;
import static org.dan.ping.pong.mock.simulator.ProvidedRank.R3;
import static org.junit.Assert.assertEquals;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.server.tournament.JerseyWithSimulator;
import org.dan.ping.pong.mock.simulator.Simulator;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.core.GenericType;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class ManualSeedJerseyTest extends AbstractSpringJerseyTest {
    @Inject
    private Simulator simulator;

    @Test
    public void reseed() {
        final TournamentScenario scenario = TournamentScenario.begin()
                .doNotBegin()
                .rules(RULES_G2Q1_S1A2G11_PRNK)
                .category(c1, p1, p2, p3)
                .rank(R1, p1)
                .rank(R2, p2)
                .rank(R3, p3)
                .name("reseed");

        simulator.simulate(scenario);
        final int cid = scenario.getCategoryDbId().get(c1);
        final List<Integer> uids = scenario.getUidPlayer().keySet().stream().sorted().collect(Collectors.toList());
        myRest().voidPost(ORDER_BIDS_MANUALLY, scenario,
                OrderCategoryBidsManually
                        .builder()
                        .cid(cid)
                        .tid(scenario.getTid())
                        .uids(uids)
                        .build());

        final List<RankedBid> result = myRest()
                .get(GET_MANUAL_BIDS_ORDER + scenario.getTid() + CID_IN + cid,
                        new GenericType<List<RankedBid>>(){});
        assertEquals(asList(1, 2, 3), result.stream().map(r -> r.getSeed().get()).collect(Collectors.toList()));
        assertEquals(uids, result.stream().map(r -> r.getUser().getUid()).collect(Collectors.toList()));
    }
}
