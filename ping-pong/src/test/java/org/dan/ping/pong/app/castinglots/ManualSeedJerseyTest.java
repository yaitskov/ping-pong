package org.dan.ping.pong.app.castinglots;

import static com.google.common.primitives.Ints.asList;
import static org.dan.ping.pong.app.bid.BidState.Lost;
import static org.dan.ping.pong.app.bid.BidState.Win1;
import static org.dan.ping.pong.app.bid.BidState.Win2;
import static org.dan.ping.pong.app.castinglots.CastingLotsResource.CID_IN;
import static org.dan.ping.pong.app.castinglots.CastingLotsResource.GET_MANUAL_BIDS_ORDER;
import static org.dan.ping.pong.app.castinglots.CastingLotsResource.ORDER_BIDS_MANUALLY;
import static org.dan.ping.pong.app.tournament.CastingLotsRulesConst.BALANCED_MANUAL;
import static org.dan.ping.pong.app.tournament.TournamentRulesConst.RULES_G2Q1_S1A2G11_NP;
import static org.dan.ping.pong.app.tournament.TournamentRulesConst.RULES_G2Q1_S1A2G11_PRNK;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.Player.p4;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.dan.ping.pong.mock.simulator.ProvidedRank.R1;
import static org.dan.ping.pong.mock.simulator.ProvidedRank.R2;
import static org.dan.ping.pong.mock.simulator.ProvidedRank.R3;
import static org.dan.ping.pong.mock.simulator.TournamentScenario.begin;
import static org.dan.ping.pong.mock.simulator.imerative.BidStatesDesc.restState;
import static org.junit.Assert.assertEquals;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.category.Cid;
import org.dan.ping.pong.app.tournament.JerseyWithSimulator;
import org.dan.ping.pong.mock.simulator.Simulator;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.dan.ping.pong.mock.simulator.imerative.ImperativeSimulatorFactory;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.ws.rs.core.GenericType;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class ManualSeedJerseyTest extends AbstractSpringJerseyTest {
    @Inject
    private Simulator simulator;

    @Test
    public void reseed() {
        final TournamentScenario scenario = begin()
                .doNotBegin()
                .rules(RULES_G2Q1_S1A2G11_PRNK)
                .category(c1, p1, p2, p3)
                .rank(R1, p1)
                .rank(R2, p2)
                .rank(R3, p3)
                .name("reseed");

        simulator.simulate(scenario);
        final Cid cid = scenario.getCategoryDbId().get(c1);
        final List<Bid> bids = scenario.getBidPlayer().keySet()
                .stream().sorted().collect(Collectors.toList());
        myRest().voidPost(ORDER_BIDS_MANUALLY, scenario,
                OrderCategoryBidsManually
                        .builder()
                        .cid(cid)
                        .tid(scenario.getTid())
                        .bids(bids)
                        .build());

        final List<RankedBid> result = myRest()
                .get(GET_MANUAL_BIDS_ORDER + scenario.getTid().getTid() + CID_IN + cid,
                        new GenericType<List<RankedBid>>(){});
        assertEquals(asList(1, 2, 3), result.stream()
                .map(r -> r.getSeed().get()).collect(Collectors.toList()));
        assertEquals(bids, result.stream().map(r -> r.getUser().getBid())
                .collect(Collectors.toList()));
    }

    @Inject
    private ImperativeSimulatorFactory isf;

    @Test
    public void splitBy2Groups() {
        final TournamentScenario scenario = begin().name("splitBy2Groups")
                .rules(RULES_G2Q1_S1A2G11_NP.withCasting(BALANCED_MANUAL))
                .category(c1, p1, p2, p3, p4);
        isf.create(scenario)
                .run(c -> {
                    myRest().voidPost(ORDER_BIDS_MANUALLY, scenario,
                            OrderCategoryBidsManually
                                    .builder()
                                    .cid(scenario.getCategoryDbId().get(c1))
                                    .tid(scenario.getTid())
                                    .bids(Stream.of(p3, p4, p1, p2)
                                            .map(scenario::player2Bid)
                                            .collect(Collectors.toList()))
                                    .build());
                    c.beginTournament()
                            .scoreSet(p1, 11, p4, 5)
                            .scoreSet(p2, 11, p3, 5)
                            .scoreSet(p1, 11, p2, 3)
                            .checkTournamentComplete(restState(Lost)
                                    .bid(p1, Win1)
                                    .bid(p2, Win2));
                });
    }
}
