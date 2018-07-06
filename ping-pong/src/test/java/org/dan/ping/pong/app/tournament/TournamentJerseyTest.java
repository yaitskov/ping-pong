package org.dan.ping.pong.app.tournament;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.Family.SUCCESSFUL;
import static javax.ws.rs.core.Response.Status.Family.familyOf;
import static javax.ws.rs.core.Response.Status.OK;
import static org.dan.ping.pong.app.auth.AuthService.SESSION;
import static org.dan.ping.pong.app.bid.BidState.Quit;
import static org.dan.ping.pong.app.bid.BidState.Want;
import static org.dan.ping.pong.app.category.CategoryResource.CATEGORY_MEMBERS;
import static org.dan.ping.pong.app.place.PlaceMemState.NO_ADMIN_ACCESS_TO_PLACE;
import static org.dan.ping.pong.app.place.PlaceMemState.PID;
import static org.dan.ping.pong.app.tournament.ResignTournament.resignOfTid;
import static org.dan.ping.pong.app.tournament.TournamentResource.BEGIN_TOURNAMENT;
import static org.dan.ping.pong.app.tournament.TournamentResource.DRAFTING;
import static org.dan.ping.pong.app.tournament.TournamentResource.EDITABLE_TOURNAMENTS;
import static org.dan.ping.pong.app.tournament.TournamentResource.RUNNING_TOURNAMENTS;
import static org.dan.ping.pong.app.tournament.TournamentResource.TOURNAMENT_CREATE;
import static org.dan.ping.pong.app.tournament.TournamentResource.TOURNAMENT_ENLIST;
import static org.dan.ping.pong.app.tournament.TournamentResource.TOURNAMENT_ENLISTED;
import static org.dan.ping.pong.app.tournament.TournamentResource.TOURNAMENT_RESIGN;
import static org.dan.ping.pong.app.tournament.TournamentResource.TOURNAMENT_STATE;
import static org.dan.ping.pong.app.tournament.TournamentRulesConst.RULES_G2Q1_S1A2G11;
import static org.dan.ping.pong.app.tournament.TournamentService.UNKNOWN_PLACE;
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
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.app.category.CategoryInfo;
import org.dan.ping.pong.app.city.CityLink;
import org.dan.ping.pong.app.place.ForTestPlaceDao;
import org.dan.ping.pong.app.place.Pid;
import org.dan.ping.pong.app.place.PlaceAddress;
import org.dan.ping.pong.app.place.PlaceDao;
import org.dan.ping.pong.mock.DaoEntityGeneratorWithAdmin;
import org.dan.ping.pong.mock.RestEntityGenerator;
import org.dan.ping.pong.mock.TestAdmin;
import org.dan.ping.pong.mock.TestUserSession;
import org.dan.ping.pong.mock.UserSessionGenerator;
import org.dan.ping.pong.mock.simulator.Simulator;
import org.dan.ping.pong.sys.error.TemplateError;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class TournamentJerseyTest extends AbstractSpringJerseyTest {
    static class DigestList extends ArrayList<TournamentDigest> {}

    @Inject
    @Named(ADMIN_SESSION)
    private TestAdmin adminSession;

    @Inject
    private RestEntityGenerator restEntityGenerator;

    @Inject
    private DaoEntityGeneratorWithAdmin daoGenerator;

    @Test
    public void failsToCreateTournamentWithUnknownPid() {
        final String name = genStr();
        final Instant opensAt = genFutureTime();
        final Pid badPid = new Pid(1111111);
        final Response response = createTournament(name, opensAt, badPid);
        assertEquals(400, response.getStatus());
        final TemplateError error = response.readEntity(TemplateError.class);
        assertThat(error, allOf(
                hasProperty("message", is(UNKNOWN_PLACE)),
                hasProperty("params", hasEntry(PID, badPid.getPid()))
        ));
    }

    @Inject
    private ForTestPlaceDao forTestPlaceDao;

    @Test
    public void failsToCreateTournamentWithForeignPid() {
        final String name = genStr();
        final Instant opensAt = genFutureTime();
        final Pid badPid = daoGenerator.genPlace(0);
        forTestPlaceDao.revokeAdmin(badPid, adminSession.getUid());
        final Response response = createTournament(name, opensAt, badPid);
        assertEquals(403, response.getStatus());
        final TemplateError error = response.readEntity(TemplateError.class);
        assertThat(error, allOf(
                hasProperty("message", is(NO_ADMIN_ACCESS_TO_PLACE)),
                hasProperty("params", hasEntry(PID, badPid.getPid()))
        ));
    }

    @Test
    public void createTournament() {
        final String name = genStr();
        final Instant opensAt = genFutureTime();
        final Response response = createTournament(name, opensAt, daoGenerator.genPlace(0));
        final Tid tid = response.readEntity(Tid.class);

        final List<TournamentDigest> digest = request().path(EDITABLE_TOURNAMENTS)
                .request(APPLICATION_JSON)
                .header(SESSION, adminSession.getSession())
                .get(DigestList.class);

        assertThat(digest, hasItem(allOf(
                hasProperty("tid", is(tid)),
                hasProperty("name", is(name)),
                hasProperty("opensAt", is(opensAt)))));
    }

    private Response createTournament(String name, Instant opensAt, Pid placeId) {
        return myRest().post(TOURNAMENT_CREATE,
                adminSession,
                CreateTournament.builder()
                        .name(name)
                        .placeId(placeId)
                        .opensAt(opensAt)
                        .previousTid(Optional.empty())
                        .rules(RULES_G2Q1_S1A2G11)
                        .ticketPrice(Optional.empty())
                        .build());
    }

    @Inject
    @Named(USER_SESSION)
    private TestUserSession userSession;

    @Test
    public void enlist() {
        final Tid tid = daoGenerator.genTournament(
                daoGenerator.genPlace(0), Draft);

        assertEquals(OK.getStatusCode(),
                request().path(TOURNAMENT_ENLIST).request(APPLICATION_JSON)
                        .header(SESSION, userSession.getSession())
                        .post(Entity.entity(EnlistTournament.builder()
                                .tid(tid)
                                .categoryId(daoGenerator.genCategory(tid, 1))
                                .build(), APPLICATION_JSON))
                        .getStatus());

        final List<TournamentDigest> digests = request()
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
        final int cityId = daoGenerator.genCity();
        final Pid placeId = placeDao.createAndGrant(adminSession.getUid(), placeName,
                PlaceAddress.builder()
                        .address(address)
                        .city(CityLink.builder().id(cityId).build())
                        .phone(Optional.of(phone))
                        .build());
        final Tid tid = daoGenerator.genTournament(placeId);
        final int cid = daoGenerator.genCategory(tid, 1);

        final DraftingTournamentInfo adminResult = myRest().get(DRAFTING + tid.getTid(),
                adminSession, DraftingTournamentInfo.class);
        assertEquals(emptyMap(), adminResult.getCategoryState());
        assertTrue(adminResult.isIAmAdmin());
        assertThat(adminResult.getCategories(), hasSize(greaterThan(0)));

        assertEquals(Optional.empty(), adminResult.getTicketPrice());
        assertThat(adminResult.getName(), Matchers.notNullValue());
        assertEquals(placeName, adminResult.getPlace().getName());
        assertEquals(address, adminResult.getPlace().getAddress().getAddress());
        assertEquals(Optional.of(phone), adminResult.getPlace().getAddress().getPhone());

        final DraftingTournamentInfo userResultBefore = myRest().get(DRAFTING + tid,
                userSession, DraftingTournamentInfo.class);
        assertEquals(emptyMap(), userResultBefore.getCategoryState());
        assertFalse(userResultBefore.isIAmAdmin());

        myRest().voidPost(TOURNAMENT_ENLIST, userSession,
                EnlistTournament.builder()
                        .tid(tid)
                        .categoryId(cid)
                        .build());

        final DraftingTournamentInfo userResultAfter = myRest().get(DRAFTING + tid,
                userSession, DraftingTournamentInfo.class);
        assertEquals(singletonMap(cid, Want), userResultAfter.getCategoryState());
        assertFalse(userResultAfter.isIAmAdmin());

        final DraftingTournamentInfo anonymousResult = myRest().get(DRAFTING + tid,
                DraftingTournamentInfo.class);
        assertEquals(emptyMap(), anonymousResult.getCategoryState());
        assertFalse(anonymousResult.isIAmAdmin());
        assertThat(adminResult.getCategories(), hasSize(greaterThan(0)));
    }

    @Test
    public void resignReenlistToOtherCategory() {
        final Tid tid = daoGenerator.genTournament(
                daoGenerator.genPlace(0), Draft);
        final int cid1 = daoGenerator.genCategory(tid, 1);
        final int cid2 = daoGenerator.genCategory(tid, 2);
        myRest().voidPost(TOURNAMENT_ENLIST, userSession, EnlistTournament.builder()
                .tid(tid)
                .categoryId(cid1)
                .build());
        final List<TournamentDigest> digests = myRest().get(TOURNAMENT_ENLISTED,
                userSession, DigestList.class);
        assertThat(digests, hasItem(hasProperty("tid", is(tid))));
        myRest().voidPost(TOURNAMENT_RESIGN, userSession, resignOfTid(tid));
        assertThat(myRest().get(TOURNAMENT_ENLISTED,
                userSession, DigestList.class),
                not(hasItem(hasProperty("tid", is(tid)))));

        final DraftingTournamentInfo afterResign = myRest().get(DRAFTING + tid,
                userSession, DraftingTournamentInfo.class);
        assertEquals(singletonMap(cid1, Quit), afterResign.getCategoryState());

        myRest().voidPost(TOURNAMENT_ENLIST, userSession, EnlistTournament.builder()
                .tid(tid)
                .categoryId(cid2)
                .build());
        assertThat(myRest().get(TOURNAMENT_ENLISTED,
                userSession, DigestList.class),
                hasItem(hasProperty("tid", is(tid))));
    }

    @Test
    public void draftingBadState() {
        final Tid tid = daoGenerator.genTournament(daoGenerator.genPlace(0), Open);
        assertThat(myRest().get(DRAFTING + tid, DraftingTournamentInfo.class),
                hasProperty("state", is(Open)));
        setTournamentState(tid, Close);
        assertThat(
                myRest().get(DRAFTING + tid, DraftingTournamentInfo.class).getState(),
                is(Close));
    }

    @Inject
    UserSessionGenerator userSessionGenerator;

    @Test
    public void runningTournaments() {
        final Pid placeId = daoGenerator.genPlace(1);
        final Tid tid = daoGenerator.genTournament(placeId, Draft, 1);
        final int cid = daoGenerator.genCategory(tid, 1);

        final List<TestUserSession> participants = userSessionGenerator.generateUserSessions(2);
        restEntityGenerator.participantsEnlistThemselves(myRest(), adminSession, tid, cid, c1, participants);

        final List<OpenTournamentDigest> openBefore = myRest()
                .get(RUNNING_TOURNAMENTS, new GenericType<List<OpenTournamentDigest>>() {});
        assertThat(openBefore, not(hasItem(hasProperty("tid", is(tid)))));
        final Response response = myRest().post(BEGIN_TOURNAMENT, adminSession.getSession(), tid);
        assertEquals(SUCCESSFUL, familyOf(response.getStatus()));
        final List<OpenTournamentDigest> openAfter = myRest()
                .get(RUNNING_TOURNAMENTS, new GenericType<List<OpenTournamentDigest>>() {});
        assertThat(openAfter, hasItem(hasProperty("tid", is(tid))));
    }

    private void setTournamentState(Tid previousTid, TournamentState state) {
        myRest().voidPost(TOURNAMENT_STATE, adminSession,
                SetTournamentState.builder()
                        .tid(previousTid)
                        .state(state)
                        .build());
    }

    @Inject
    private Simulator simulator;

    @Test
    public void enlistOffline() {
        final Tid tid = daoGenerator.genTournament(
                daoGenerator.genPlace(0), Draft);
        final int cid = daoGenerator.genCategory(tid, 1);
        final String name = UUID.randomUUID().toString();
        final Bid bid = simulator.enlistNewParticipant(tid, adminSession, cid, Optional.empty(), name,
                BidState.Want);
        final CategoryInfo digests = myRest().get(
                CATEGORY_MEMBERS + tid.getTid() + "/" + cid, CategoryInfo.class);
        assertThat(digests.getUsers(),
                hasItem(allOf(
                        hasProperty("name", is(name)),
                        hasProperty("bid", is(bid)))));
    }
}
