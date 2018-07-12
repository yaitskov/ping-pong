package org.dan.ping.pong.app.table;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.dan.ping.pong.app.match.MatchResource.MY_PENDING_MATCHES;
import static org.dan.ping.pong.app.match.MatchState.Game;
import static org.dan.ping.pong.app.tournament.TournamentResource.BEGIN_TOURNAMENT;
import static org.dan.ping.pong.app.tournament.TournamentState.Draft;
import static org.dan.ping.pong.mock.AdminSessionGenerator.ADMIN_SESSION;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.category.Cid;
import org.dan.ping.pong.app.match.MyPendingMatch;
import org.dan.ping.pong.app.match.MyPendingMatchList;
import org.dan.ping.pong.app.place.Pid;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.mock.DaoEntityGeneratorWithAdmin;
import org.dan.ping.pong.mock.RestEntityGenerator;
import org.dan.ping.pong.mock.TestAdmin;
import org.dan.ping.pong.mock.TestUserSession;
import org.dan.ping.pong.mock.UserSessionGenerator;
import org.dan.ping.pong.sys.ctx.TestCtx;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Response;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = TestCtx.class)
public class TableJerseyTest extends AbstractSpringJerseyTest {
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
    public void locateOneGameOnOneTable() {
        final Pid placeId = daoGenerator.genPlace(1);
        final Tid tid = daoGenerator.genTournament(placeId, Draft, 1);
        final Cid cid = daoGenerator.genCategory(tid, Cid.of(1));
        final List<TestUserSession> participants = userSessionGenerator.generateUserSessions(2);
        restEntityGenerator.participantsEnlistThemselves(
                myRest(), adminSession, tid, cid, c1, participants);
        myRest().voidPost(BEGIN_TOURNAMENT, adminSession, tid);
        final List<MyPendingMatch> c = participants.stream().map(p -> findMatch(tid, p).getMatches())
                .flatMap(List::stream)
                .collect(toList());
        assertThat(c.stream().map(MyPendingMatch::getTable).collect(toSet()),
                hasSize(is(1)));
        assertEquals(asList(Game, Game), c.stream()
                        .map(MyPendingMatch::getState)
                        .collect(toList()));
    }

    @Test
    public void locateOneGameWithoutTables() {
        final Pid placeId = daoGenerator.genPlace(0);
        final Tid tid = daoGenerator.genTournament(placeId, Draft, 1);
        final Cid cid = daoGenerator.genCategory(tid, Cid.of(1));
        final List<TestUserSession> participants = userSessionGenerator.generateUserSessions(2);
        restEntityGenerator.participantsEnlistThemselves(myRest(),
                adminSession, tid, cid, c1, participants);
        final Response response = myRest().post(BEGIN_TOURNAMENT, adminSession.getSession(), tid);
        assertThat(response.getStatus(), is(400));
        assertThat(response.readEntity(String.class), containsString("doesn't have any table"));
    }

    private MyPendingMatchList findMatch(Tid tid, TestUserSession participant) {
        return myRest().get(MY_PENDING_MATCHES + tid.getTid(),
                participant,
                MyPendingMatchList.class);
    }
}
