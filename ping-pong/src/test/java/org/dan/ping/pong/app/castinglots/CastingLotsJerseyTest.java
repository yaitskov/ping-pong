package org.dan.ping.pong.app.castinglots;

import static java.util.stream.Collectors.toSet;
import static org.dan.ping.pong.app.match.MatchResource.MY_PENDING_MATCHES;
import static org.dan.ping.pong.app.match.MatchState.Place;
import static org.dan.ping.pong.app.match.MatchType.Group;
import static org.dan.ping.pong.app.tournament.TournamentState.Draft;
import static org.dan.ping.pong.mock.AdminSessionGenerator.ADMIN_SESSION;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.bid.BidCtx;
import org.dan.ping.pong.app.group.GroupCtx;
import org.dan.ping.pong.app.match.MatchCtx;
import org.dan.ping.pong.app.match.MyPendingMatch;
import org.dan.ping.pong.app.score.MatchScoreCtx;
import org.dan.ping.pong.app.table.TableCtx;
import org.dan.ping.pong.app.tournament.TournamentCtx;
import org.dan.ping.pong.app.user.UserLink;
import org.dan.ping.pong.mock.DaoEntityGeneratorWithAdmin;
import org.dan.ping.pong.mock.RestEntityGenerator;
import org.dan.ping.pong.mock.TestAdmin;
import org.dan.ping.pong.mock.TestUserSession;
import org.dan.ping.pong.mock.UserSessionGenerator;
import org.dan.ping.pong.sys.ctx.BaseTestContext;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.GenericType;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = {BaseTestContext.class,
        TournamentCtx.class, BidCtx.class, TableCtx.class,
        CastingLotsCtx.class, MatchCtx.class, MatchScoreCtx.class,
        GroupCtx.class})
public class CastingLotsJerseyTest extends AbstractSpringJerseyTest {
    @Inject
    @Named(ADMIN_SESSION)
    private TestAdmin adminSession;

    @Inject
    private RestEntityGenerator restEntityGenerator;

    @Inject
    private DaoEntityGeneratorWithAdmin daoGenerator;

    @Inject
    private UserSessionGenerator userSessionGenerator;

    @Test
    public void makeGroupsOf1Bid() {
        // fail - not enough participants
    }

    @Test
    public void makeGroupsOf2Bids() {
        final int placeId = daoGenerator.genPlace(0);
        final int tid = daoGenerator.genTournament(placeId, Draft, 1);
        final List<TestUserSession> participants =  restEntityGenerator.generateGroupsOf(
                myRest(), adminSession, userSessionGenerator, tid, 2);
        final Set<Integer> participantIds = participants.stream()
                .map(TestUserSession::getUid).collect(toSet());
        for (TestUserSession userSession : participants) {
            final List<MyPendingMatch> matches = myRest().get(MY_PENDING_MATCHES,
                    userSession, new GenericType<List<MyPendingMatch>>(){});
            assertThat(matches.stream().map(MyPendingMatch::getState).collect(toSet()),
                    is(ImmutableSet.of(Place)));
            assertThat(matches.stream().map(MyPendingMatch::getMatchType).collect(toSet()),
                    is(ImmutableSet.of(Group)));
            assertThat(matches.stream().map(MyPendingMatch::getEnemy)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .map(UserLink::getUid)
                            .collect(toSet()),
                    is(participantIds.stream().filter(id -> !id.equals(userSession.getUid()))
                            .collect(toSet())));
        }
    }

    @Test
    public void makeGroupsOf3Bids() {
    }

    @Test
    public void makeGroupsOf9Bids() {
    }

    @Test
    public void makeGroupsOf16Bids() {
    }
}
