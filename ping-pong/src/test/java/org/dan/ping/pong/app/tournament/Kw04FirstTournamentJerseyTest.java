package org.dan.ping.pong.app.tournament;

import static org.dan.ping.pong.app.castinglots.CastingLotsResource.ORDER_BIDS_MANUALLY;
import static org.dan.ping.pong.app.castinglots.MatchScheduleInGroupJerseyTest.G8Q1;
import static org.dan.ping.pong.app.castinglots.MatchScheduleInGroupJerseyTest.S1A2G11;
import static org.dan.ping.pong.app.match.MatchResource.MATCH_WATCH_LIST_OPEN;
import static org.dan.ping.pong.app.tournament.TournamentResource.BEGIN_TOURNAMENT;
import static org.dan.ping.pong.app.tournament.TournamentResource.TOURNAMENT_EXPEL;
import static org.dan.ping.pong.app.tournament.TournamentResource.TOURNAMENT_INVALIDATE_CACHE;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.Player.p4;
import static org.dan.ping.pong.mock.simulator.Player.p5;
import static org.dan.ping.pong.mock.simulator.Player.p6;
import static org.dan.ping.pong.mock.simulator.Player.p7;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.junit.Assert.assertEquals;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.castinglots.OrderCategoryBidsManually;
import org.dan.ping.pong.app.castinglots.rank.CastingLotsRule;
import org.dan.ping.pong.app.castinglots.rank.GroupSplitPolicy;
import org.dan.ping.pong.app.castinglots.rank.OrderDirection;
import org.dan.ping.pong.app.castinglots.rank.ParticipantRankingPolicy;
import org.dan.ping.pong.app.match.OpenMatchForWatch;
import org.dan.ping.pong.mock.simulator.Simulator;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.core.GenericType;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class Kw04FirstTournamentJerseyTest extends AbstractSpringJerseyTest {
    @Inject
    private Simulator simulator;

    public static final TournamentRules RULES_G8Q1_S1A2G11 = TournamentRules
            .builder()
            .match(S1A2G11)
            .group(Optional.of(G8Q1))
            .casting(CastingLotsRule.builder()
                    .policy(ParticipantRankingPolicy.Manual)
                    .direction(OrderDirection.Decrease)
                    .splitPolicy(GroupSplitPolicy.BalancedMix)
                    .providedRankOptions(Optional.empty())
                    .build())
            .playOff(Optional.empty())
            .build();


    @Test
    public void countTablesWith6OfThem() {
        final TournamentScenario scenario = TournamentScenario.begin()
                .doNotBegin()
                .tables(6)
                .rules(RULES_G8Q1_S1A2G11)
                .category(c1, p1, p2, p3, p4, p5, p6, p7)
                .name("kw04-tournament-1");

        simulator.simulate(scenario);

        myRest().voidPost(ORDER_BIDS_MANUALLY, scenario,
                OrderCategoryBidsManually
                        .builder()
                        .uids(scenario.getUidPlayer().keySet().stream()
                                .sorted(Comparator.comparing(Uid::getId).reversed())
                                .collect(Collectors.toList()))
                        .cid(scenario.getCategoryDbId().get(c1))
                        .tid(scenario.getTid())
                        .build());

        myRest().voidPost(TOURNAMENT_EXPEL, scenario, ExpelParticipant
                .builder()
                .tid(scenario.getTid())
                .uid(scenario.getPlayersSessions().get(p1).getUid())
                .build());

        myRest().voidPost(BEGIN_TOURNAMENT, scenario, scenario.getTid());

        myRest().voidPost(TOURNAMENT_INVALIDATE_CACHE, scenario, scenario.getTid());

        final List<OpenMatchForWatch> openMatches = myRest()
                .get(MATCH_WATCH_LIST_OPEN + scenario.getTid().getTid(),
                scenario, new GenericType<List<OpenMatchForWatch>>() {});
        assertEquals(openMatches.size(), 15);
    }
}
