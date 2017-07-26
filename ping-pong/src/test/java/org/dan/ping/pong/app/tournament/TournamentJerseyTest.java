package org.dan.ping.pong.app.tournament;

import static java.util.Collections.singletonList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.Family.SUCCESSFUL;
import static javax.ws.rs.core.Response.Status.Family.familyOf;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static org.dan.ping.pong.app.auth.AuthService.SESSION;
import static org.dan.ping.pong.app.tournament.TournamentResource.BEGIN_TOURNAMENT;
import static org.dan.ping.pong.app.tournament.TournamentResource.DRAFTING;
import static org.dan.ping.pong.app.tournament.TournamentResource.EDITABLE_TOURNAMENTS;
import static org.dan.ping.pong.app.tournament.TournamentResource.MY_RECENT_TOURNAMENT;
import static org.dan.ping.pong.app.tournament.TournamentResource.RUNNING_TOURNAMENTS;
import static org.dan.ping.pong.app.tournament.TournamentResource.TOURNAMENT_CREATE;
import static org.dan.ping.pong.app.tournament.TournamentResource.TOURNAMENT_ENLIST;
import static org.dan.ping.pong.app.tournament.TournamentResource.TOURNAMENT_ENLISTED;
import static org.dan.ping.pong.app.tournament.TournamentResource.TOURNAMENT_RESIGN;
import static org.dan.ping.pong.app.tournament.TournamentState.Close;
import static org.dan.ping.pong.app.tournament.TournamentState.Draft;
import static org.dan.ping.pong.app.tournament.TournamentState.Open;
import static org.dan.ping.pong.mock.AdminSessionGenerator.ADMIN_SESSION;
import static org.dan.ping.pong.mock.Generators.genFutureTime;
import static org.dan.ping.pong.mock.Generators.genPhone;
import static org.dan.ping.pong.mock.Generators.genPlaceLocation;
import static org.dan.ping.pong.mock.Generators.genPlaceName;
import static org.dan.ping.pong.mock.Generators.genStr;
import static org.dan.ping.pong.mock.UserSessionGenerator.USER_SESSION;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.bid.BidDao;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.app.place.PlaceAddress;
import org.dan.ping.pong.app.place.PlaceDao;
import org.dan.ping.pong.mock.DaoEntityGeneratorWithAdmin;
import org.dan.ping.pong.mock.RestEntityGenerator;
import org.dan.ping.pong.mock.TestAdmin;
import org.dan.ping.pong.mock.TestUserSession;
import org.dan.ping.pong.mock.TournamentProps;
import org.dan.ping.pong.mock.UserSessionGenerator;
import org.dan.ping.pong.sys.ctx.TestCtx;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.dan.ping.pong.util.time.Clocker;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = TestCtx.class)
public class TournamentJerseyTest extends AbstractSpringJerseyTest {
    static class DigestList extends ArrayList<DatedTournamentDigest> {}

    @Inject
    @Named(ADMIN_SESSION)
    private TestAdmin session;

    @Inject
    private RestEntityGenerator restEntityGenerator;

    @Inject
    private DaoEntityGeneratorWithAdmin daoGenerator;

    @Test
    public void createTournament() {
        final String name = genStr();
        final Instant opensAt = genFutureTime();
        final Response response = request().path(TOURNAMENT_CREATE)
                .request(APPLICATION_JSON)
                .header(SESSION, session.getSession())
                .post(Entity.entity(CreateTournament.builder()
                        .name(name)
                        .placeId(daoGenerator.genPlace(0))
                        .opensAt(opensAt)
                        .previousTid(Optional.empty())
                        .quitsFromGroup(2)
                        .ticketPrice(Optional.empty())
                        .thirdPlaceMatch(0)
                        .matchScore(3)
                        .build(), APPLICATION_JSON));
        final int tid = response.readEntity(Integer.class);

        final List<DatedTournamentDigest> digest = request().path(EDITABLE_TOURNAMENTS)
                .request(APPLICATION_JSON)
                .header(SESSION, session.getSession())
                .get(DigestList.class);

        assertThat(digest, hasItem(allOf(
                        hasProperty("tid", is(tid)),
                        hasProperty("name", is(name)),
                        hasProperty("opensAt", is(opensAt)))));
    }

    @Inject
    @Named(USER_SESSION)
    private TestUserSession userSession;

    @Test
    public void enlist() {
        final int tid = daoGenerator.genTournament(
                daoGenerator.genPlace(0), Draft);

        assertEquals(NO_CONTENT.getStatusCode(),
                request().path(TOURNAMENT_ENLIST).request(APPLICATION_JSON)
                        .header(SESSION, userSession.getSession())
                        .post(Entity.entity(EnlistTournament.builder()
                                .tid(tid)
                                .categoryId(daoGenerator.genCategory(tid))
                                .build(), APPLICATION_JSON))
                        .getStatus());

        final List<DatedTournamentDigest> digests = request()
                .path(TOURNAMENT_ENLISTED)
                .request(APPLICATION_JSON)
                .header(SESSION, userSession.getSession())
                .get(DigestList.class);
        assertThat(digests, hasItem(hasProperty("tid", is(tid))));
    }

    @Inject
    private PlaceDao placeDao;

    @Test
    public void getDraftingTournament() {
        final String placeName = genPlaceName();
        final String address = genPlaceLocation();
        final String phone = genPhone();
        final int placeId = placeDao.createAndGrant(session.getUid(), placeName,
                PlaceAddress.builder()
                        .address(address)
                        .phone(Optional.of(phone))
                        .build());
        final int tid = daoGenerator.genTournament(placeId);
        final int cid = daoGenerator.genCategory(tid);

        final DraftingTournamentInfo adminResult = myRest().get(DRAFTING + tid,
                session, DraftingTournamentInfo.class);
        assertEquals(0, adminResult.getAlreadyEnlisted());
        assertFalse(adminResult.getMyCategoryId().isPresent());
        assertTrue(adminResult.isIAmAdmin());
        assertThat(adminResult.getCategories(), hasSize(greaterThan(0)));

        assertEquals(Optional.empty(), adminResult.getTicketPrice());
        assertThat(adminResult.getName(), Matchers.notNullValue());
        assertEquals(placeName, adminResult.getPlace().getName());
        assertEquals(address, adminResult.getPlace().getAddress().getAddress());
        assertEquals(Optional.of(phone), adminResult.getPlace().getAddress().getPhone());

        final DraftingTournamentInfo userResultBefore = myRest().get(DRAFTING + tid,
                userSession, DraftingTournamentInfo.class);
        assertEquals(0, userResultBefore.getAlreadyEnlisted());
        assertFalse(userResultBefore.getMyCategoryId().isPresent());
        assertFalse(userResultBefore.isIAmAdmin());

        myRest().voidPost(TOURNAMENT_ENLIST, userSession,
                EnlistTournament.builder()
                        .tid(tid)
                        .categoryId(cid)
                        .build());

        final DraftingTournamentInfo userResultAfter = myRest().get(DRAFTING + tid,
                userSession, DraftingTournamentInfo.class);
        assertEquals(1, userResultAfter.getAlreadyEnlisted());
        assertTrue(userResultAfter.getMyCategoryId().isPresent());
        assertFalse(userResultAfter.isIAmAdmin());

        final DraftingTournamentInfo anonymousResult = myRest().get(DRAFTING + tid,
                DraftingTournamentInfo.class);
        assertEquals(1, anonymousResult.getAlreadyEnlisted());
        assertFalse(anonymousResult.getMyCategoryId().isPresent());
        assertFalse(anonymousResult.isIAmAdmin());
        assertThat(adminResult.getCategories(), hasSize(greaterThan(0)));
    }

    @Test
    public void resign() {
        final int tid = daoGenerator.genTournament(
                daoGenerator.genPlace(0), Draft);
        myRest().voidPost(TOURNAMENT_ENLIST, userSession, EnlistTournament.builder()
                                .tid(tid)
                                .categoryId(daoGenerator.genCategory(tid))
                                .build());
        final List<DatedTournamentDigest> digests = myRest().get(TOURNAMENT_ENLISTED,
                userSession, DigestList.class);
        assertThat(digests, hasItem(hasProperty("tid", is(tid))));
        myRest().voidPost(TOURNAMENT_RESIGN, userSession, tid);
        assertThat(myRest().get(TOURNAMENT_ENLISTED,
                userSession, DigestList.class),
                not(hasItem(hasProperty("tid", is(tid)))));
        myRest().voidPost(TOURNAMENT_ENLIST, userSession, EnlistTournament.builder()
                .tid(tid)
                .categoryId(daoGenerator.genCategory(tid))
                .build());
        assertThat(myRest().get(TOURNAMENT_ENLISTED,
                userSession, DigestList.class),
                hasItem(hasProperty("tid", is(tid))));
    }

    @Test
    public void draftingBadState() {
        final int tid = daoGenerator.genTournament(daoGenerator.genPlace(0), Open);
        try {
            myRest().get(DRAFTING + tid, DraftingTournamentInfo.class);
            fail();
        } catch (WebApplicationException e) {
            BadTournamentState error = e.getResponse().readEntity(BadTournamentState.class);
            assertThat(error.getState(), is(Open));
        }
    }

    @Inject
    UserSessionGenerator userSessionGenerator;

    @Test
    public void runningTournaments() {
        final int placeId = daoGenerator.genPlace(1);
        final int tid = daoGenerator.genTournament(placeId,
                TournamentProps.builder().state(Draft).build());
        final int cid = daoGenerator.genCategory(tid);

        final List<TestUserSession> participants = userSessionGenerator.generateUserSessions(2);
        restEntityGenerator.enlistParticipants(myRest(), session, tid, cid, participants);

        final List<OpenTournamentDigest> openBefore = myRest()
                .get(RUNNING_TOURNAMENTS, new GenericType<List<OpenTournamentDigest>>() {});
        assertThat(openBefore, not(hasItem(hasProperty("tid", is(tid)))));
        final Response response = myRest().post(BEGIN_TOURNAMENT, session.getSession(), tid);
        assertEquals(SUCCESSFUL, familyOf(response.getStatus()));
        final List<OpenTournamentDigest> openAfter = myRest()
                .get(RUNNING_TOURNAMENTS, new GenericType<List<OpenTournamentDigest>>() {});
        assertThat(openAfter, hasItem(hasProperty("tid", is(tid))));
    }

    @Inject
    private TournamentDao tournamentDao;

    @Inject
    private Clocker clocker;

    @Inject
    private BidDao bidDao;

    @Test
    public void myRecentTournaments() {
        final int placeId = daoGenerator.genPlace(1);
        final Instant previousOpensAt = clocker.get().minus(1, ChronoUnit.DAYS);
        final int previousTid = daoGenerator.genTournament(placeId,
                TournamentProps.builder()
                        .opensAt(Optional.of(previousOpensAt))
                        .state(Draft).build());
        final int previousCid = daoGenerator.genCategory(previousTid);
        final Instant nextOpensAt = clocker.get().plus(3, ChronoUnit.DAYS);
        final int nextTid = daoGenerator.genTournament(placeId,
                TournamentProps.builder()
                        .opensAt(Optional.of(nextOpensAt))
                        .state(Draft).build());
        final int nextCid = daoGenerator.genCategory(nextTid);
        restEntityGenerator.enlistParticipants(myRest(), session,
                previousTid, previousCid, singletonList(userSession));
        restEntityGenerator.enlistParticipants(myRest(), session,
                nextTid, nextCid, singletonList(userSession));
        tournamentDao.setState(previousTid, Close);
        bidDao.setBidState(previousTid, userSession.getUid(),
                BidState.Here, BidState.Lost);
        final MyRecentTournaments r = myRest().get(MY_RECENT_TOURNAMENT,
                userSession, MyRecentTournaments.class);

        assertEquals(previousTid, r.getPrevious().get().getTid());
        assertEquals(BidState.Lost, r.getPrevious().get().getOutcome());
        assertThat(r.getPrevious().get().getName(), notNullValue());

        assertEquals(nextTid, r.getNext().get().getTid());
        assertEquals(nextOpensAt, r.getNext().get().getOpensAt());
        assertThat(r.getNext().get().getName(), notNullValue());
    }
}
