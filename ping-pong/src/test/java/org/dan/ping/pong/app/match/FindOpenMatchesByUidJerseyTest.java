package org.dan.ping.pong.app.match;

import static java.util.stream.Collectors.toSet;
import static org.dan.ping.pong.app.bid.BidResource.TID_SLASH_UID;
import static org.dan.ping.pong.app.bid.BidState.Play;
import static org.dan.ping.pong.app.match.MatchResource.BID_PENDING_MATCHES;
import static org.dan.ping.pong.app.tournament.TournamentRulesConst.RULES_G8Q1_S3A2G11;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.bid.ParticipantLink;
import org.dan.ping.pong.app.tournament.JerseyWithSimulator;
import org.dan.ping.pong.mock.simulator.Simulator;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;

import javax.inject.Inject;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class FindOpenMatchesByUidJerseyTest extends AbstractSpringJerseyTest {
    @Inject
    private Simulator simulator;

    @Test
    public void findOpenMatchesByUid() {
        final TournamentScenario scenario = TournamentScenario.begin()
                .tables(0)
                .ignoreUnexpectedGames()
                .name("findByMidsByUid")
                .rules(RULES_G8Q1_S3A2G11.withPlace(Optional.empty()))
                .category(c1, p1, p2, p3);
        simulator.simulate(scenario);

        final MyPendingMatchList matches = myRest()
                .get(BID_PENDING_MATCHES + scenario.getTid().getTid()
                        + TID_SLASH_UID + scenario.player2Bid(p1).intValue(),
                        MyPendingMatchList.class);
        assertThat(matches.isShowTables(), is(false));
        assertThat(matches.getBidState(), is(Play));
        assertThat(
                matches.getMatches().stream()
                        .map(MyPendingMatch::getEnemy)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .map(ParticipantLink::getBid)
                        .collect(toSet()),
                is(ImmutableSet.of(
                        scenario.player2Bid(p2),
                        scenario.player2Bid(p3))));
    }
}
