package org.dan.ping.pong.app.castinglots;

import static java.util.stream.Collectors.toSet;
import static org.dan.ping.pong.app.match.MatchResource.MY_PENDING_MATCHES;
import static org.dan.ping.pong.app.match.MatchState.Game;
import static org.dan.ping.pong.app.match.MatchType.Grup;
import static org.dan.ping.pong.app.tournament.Kw04FirstTournamentJerseyTest.RULES_G8Q1_S1A2G11;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.match.MyPendingMatch;
import org.dan.ping.pong.app.match.MyPendingMatchList;
import org.dan.ping.pong.app.tournament.JerseyWithSimulator;
import org.dan.ping.pong.app.user.UserLink;
import org.dan.ping.pong.mock.TestUserSession;
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
public class PlayerPendingMatchesJerseyTest extends AbstractSpringJerseyTest {
    @Inject
    private Simulator simulator;

    @Test
    public void noTables2Players() {
        final TournamentScenario scenario = TournamentScenario.begin()
                .ignoreUnexpectedGames()
                .tables(0)
                .rules(RULES_G8Q1_S1A2G11)
                .category(c1, p1, p2)
                .name("noTables2Players");

        simulator.simulate(scenario);

        for (TestUserSession userSession : scenario.getPlayersSessions().values()) {
            final MyPendingMatchList matches = myRest().get(
                    MY_PENDING_MATCHES + scenario.getTid(),
                    userSession,
                    MyPendingMatchList.class);
            assertThat(matches.getTotalLeft(), is(1L));
            assertThat(matches.isShowTables(), is(false));
            assertThat(matches.getMatches().stream().map(MyPendingMatch::getState).collect(toSet()),
                    is(ImmutableSet.of(Game)));
            assertThat(matches.getMatches().stream().map(MyPendingMatch::getMatchType).collect(toSet()),
                    is(ImmutableSet.of(Grup)));
            assertThat(matches.getMatches().stream().map(MyPendingMatch::getTable).collect(toSet()),
                    is(ImmutableSet.of(Optional.empty())));
            assertThat(matches.getMatches().stream().map(MyPendingMatch::getEnemy)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .map(UserLink::getUid)
                            .collect(toSet()),
                    is(scenario.getUidPlayer().keySet().stream().filter(id -> !id.equals(userSession.getUid()))
                            .collect(toSet())));
        }
    }
}