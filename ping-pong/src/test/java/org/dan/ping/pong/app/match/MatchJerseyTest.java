package org.dan.ping.pong.app.match;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.dan.ping.pong.app.bid.BidState.Lost;
import static org.dan.ping.pong.app.bid.BidState.Play;
import static org.dan.ping.pong.app.bid.BidState.Wait;
import static org.dan.ping.pong.app.bid.BidState.Win1;
import static org.dan.ping.pong.app.match.MatchResource.COMPLETE_MATCH;
import static org.dan.ping.pong.app.match.MatchResource.MATCH_WATCH_LIST_OPEN;
import static org.dan.ping.pong.app.match.MatchState.Over;
import static org.dan.ping.pong.app.match.MatchType.Grup;
import static org.dan.ping.pong.app.table.TableState.Busy;
import static org.dan.ping.pong.app.table.TableState.Free;
import static org.dan.ping.pong.app.tournament.TournamentState.Close;
import static org.dan.ping.pong.app.tournament.TournamentState.Open;
import static org.dan.ping.pong.mock.AdminSessionGenerator.ADMIN_SESSION;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.Player.p4;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.dan.ping.pong.mock.simulator.SimulatorParams.T_1_Q_1_G_2;
import static org.dan.ping.pong.mock.simulator.SimulatorParams.T_1_Q_1_G_8;
import static org.dan.ping.pong.mock.simulator.SimulatorParams.T_1_Q_2_G_8;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.bid.BidDao;
import org.dan.ping.pong.app.score.MatchScoreDao;
import org.dan.ping.pong.app.score.ScoreInfo;
import org.dan.ping.pong.app.table.TableDao;
import org.dan.ping.pong.app.table.TableInfo;
import org.dan.ping.pong.app.tournament.TournamentDao;
import org.dan.ping.pong.app.tournament.TournamentInfo;
import org.dan.ping.pong.app.user.UserLink;
import org.dan.ping.pong.mock.DaoEntityGeneratorWithAdmin;
import org.dan.ping.pong.mock.GeneratedOpenTournament;
import org.dan.ping.pong.mock.MyLocalRest;
import org.dan.ping.pong.mock.OpenTournamentGenerator;
import org.dan.ping.pong.mock.OpenTournamentParams;
import org.dan.ping.pong.mock.RestEntityGeneratorWithAdmin;
import org.dan.ping.pong.mock.TestAdmin;
import org.dan.ping.pong.mock.TestUserSession;
import org.dan.ping.pong.mock.TournamentProps;
import org.dan.ping.pong.mock.UserSessionGenerator;
import org.dan.ping.pong.mock.simulator.Simulator;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.dan.ping.pong.sys.ctx.TestCtx;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.GenericType;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = {TestCtx.class, ForTestMatchDao.class,
        ForTestBidDao.class, Simulator.class, OpenTournamentGenerator.class})
public class MatchJerseyTest extends AbstractSpringJerseyTest {
    private static final int LOSER = 0;
    private static final int WINER = 1;
    private static final int NEXT = 2;

    private static final int WINNER_SCORE = 3;
    private static final int LOSER_SCORE = 1;
    @Inject
    @Named(ADMIN_SESSION)
    private TestAdmin adminSession;

    @Inject
    private RestEntityGeneratorWithAdmin restGenerator;

    @Inject
    private DaoEntityGeneratorWithAdmin daoGenerator;

    @Inject
    private UserSessionGenerator userSessionGenerator;

    @Inject
    private MyLocalRest rest;

    @Inject
    private BidDao bidDao;

    @Inject
    private MatchDao matchDao;

    @Inject
    private MatchScoreDao matchScoreDao;

    @Inject
    private TournamentDao tournamentDao;

    @Inject
    private TableDao tableDao;

    @Test
    public void adminCompleteOnlyMatchIn1Group() {
        final int placeId = daoGenerator.genPlace(1);
        final int tid = daoGenerator.genTournament(placeId, TournamentProps.builder()
                .quitsFromGroup(1).build());
        final int cid = daoGenerator.genCategory(tid);
        final List<TestUserSession> participants = userSessionGenerator.generateUserSessions(2);
        restGenerator.enlistParticipants(tid, cid, participants);
        assertEquals(emptyList(), restGenerator.listOpenMatches());
        restGenerator.beginTournament(tid);

        final List<OpenMatchForJudge> adminOpenMatches = restGenerator.listOpenMatches();

        assertEquals(
                participants.stream().map(TestUserSession::getUid).collect(toSet()),
                adminOpenMatches.stream().map(OpenMatchForJudge::getParticipants)
                        .flatMap(List::stream)
                        .map(UserLink::getUid)
                        .collect(toSet()));

        rest.voidPost(COMPLETE_MATCH, adminSession,
                FinalMatchScore.builder()
                        .mid(adminOpenMatches.get(0).getMid())
                        .scores(asList(
                                IdentifiedScore.builder().score(LOSER_SCORE)
                                        .uid(participants.get(LOSER).getUid()).build(),
                                IdentifiedScore.builder().score(WINNER_SCORE)
                                        .uid(participants.get(WINER).getUid()).build()))
                        .build());

        assertEquals(Stream.of(Win1, Lost).map(Optional::of).collect(toList()),
                asList(bidDao.getState(tid, participants.get(WINER).getUid()),
                        bidDao.getState(tid, participants.get(LOSER).getUid())));

        assertEquals(Stream.of(Over, Close).map(Optional::of).collect(toList()),
                asList(matchDao.getById(adminOpenMatches.get(0).getMid())
                                .map(MatchInfo::getState),
                        tournamentDao.getById(tid).map(TournamentInfo::getState)));

        assertEquals(Stream.of(
                ScoreInfo.builder().tid(tid).uid(participants.get(WINER).getUid())
                        .score(WINNER_SCORE)
                        .won(WINNER_SCORE - LOSER_SCORE)
                        .build(),
                ScoreInfo.builder().tid(tid).uid(participants.get(LOSER).getUid())
                        .score(LOSER_SCORE)
                        .won(LOSER_SCORE - WINNER_SCORE)
                        .build())
                        .map(Optional::of).collect(toList()),
                Stream.of(WINER, LOSER).map(i ->
                        matchScoreDao.get(adminOpenMatches.get(0).getMid(),
                                participants.get(i).getUid()))
                        .collect(toList()));

        List<TableInfo> tables = tableDao.findFreeTables(tid);
        assertEquals(singletonList(Free), tables.stream().map(TableInfo::getState).collect(toList()));
        assertEquals(singletonList(Optional.empty()),
                tables.stream().map(TableInfo::getMid).collect(toList()));
        assertEquals(emptyList(), restGenerator.listOpenMatches());
    }

    @Inject
    private OpenTournamentGenerator openTournamentGenerator;

    @Test
    public void listOpenMatchesForWatch() {
        final GeneratedOpenTournament got = openTournamentGenerator.genOpenTour(
                OpenTournamentParams.builder().build());
        List<OpenMatchForWatch> result = rest.get(MATCH_WATCH_LIST_OPEN + "/" + got.getTid(),
                new GenericType<List<OpenMatchForWatch>>() {});
        assertThat(result, hasItem(allOf(
                hasProperty("mid", greaterThan(0)),
                hasProperty("type", is(Grup)),
                hasProperty("score", is(asList(0, 0))),
                hasProperty("category", allOf(
                        hasProperty("name", notNullValue()),
                        hasProperty("cid", is(got.getCid())))),
                hasProperty("participants", hasItems(
                        allOf(
                                hasProperty("name", notNullValue()),
                                hasProperty("uid", is(got.getSessions().get(1).getUid()))),
                        allOf(
                                hasProperty("name", notNullValue()),
                                hasProperty("uid", is(got.getSessions().get(0).getUid()))))),
                hasProperty("table",
                        allOf(hasProperty("label", notNullValue()),
                                hasProperty("id", is(got.getTableIds().get(0))))))));
    }

    @Test
    public void adminCompleteFirstMatchInGroup() {
        final int placeId = daoGenerator.genPlace(1);
        final int tid = daoGenerator.genTournament(placeId, TournamentProps.builder()
                .quitsFromGroup(1).build());
        final int cid = daoGenerator.genCategory(tid);
        final List<TestUserSession> participants = userSessionGenerator.generateUserSessions(3);
        restGenerator.enlistParticipants(tid, cid, participants);
        assertEquals(emptyList(), restGenerator.listOpenMatches());
        restGenerator.beginTournament(tid);

        assertEquals(emptyList(), restGenerator.listCompleteMatches(tid));
        final List<OpenMatchForJudge> adminOpenMatches = restGenerator.listOpenMatches();

        assertEquals(
                participants.stream().limit(2).map(TestUserSession::getUid).collect(toSet()),
                adminOpenMatches.stream().map(OpenMatchForJudge::getParticipants)
                        .flatMap(List::stream)
                        .map(UserLink::getUid)
                        .collect(toSet()));

        rest.voidPost(COMPLETE_MATCH, adminSession,
                FinalMatchScore.builder()
                        .mid(adminOpenMatches.get(0).getMid())
                        .scores(asList(
                                IdentifiedScore.builder().score(LOSER_SCORE)
                                        .uid(participants.get(LOSER).getUid()).build(),
                                IdentifiedScore.builder().score(WINNER_SCORE)
                                        .uid(participants.get(WINER).getUid()).build()))
                        .build());

        assertEquals(Stream.of(Wait, Play, Play).map(Optional::of).collect(toList()),
                Stream.of(WINER, LOSER, NEXT)
                        .map(i -> bidDao.getState(tid, participants.get(i).getUid()))
                        .collect(toList()));
        final List<CompleteMatch> completeMatches = restGenerator.listCompleteMatches(tid);
        assertEquals(
                singletonList(adminOpenMatches.get(0).getMid()),
                completeMatches.stream().map(CompleteMatch::getMid)
                        .collect(toList()));
        final List<OpenMatchForJudge> adminOpenMatches2 = restGenerator.listOpenMatches();
        assertNotEquals(
                singletonList(adminOpenMatches2.get(0).getMid()),
                completeMatches.stream().map(CompleteMatch::getMid)
                        .collect(toList()));
        assertEquals(Optional.of(Open),
                tournamentDao.getById(tid).map(TournamentInfo::getState));
        assertEquals(Stream.of(
                ScoreInfo.builder().tid(tid).uid(participants.get(WINER).getUid())
                        .score(WINNER_SCORE)
                        .won(WINNER_SCORE - LOSER_SCORE)
                        .build(),
                ScoreInfo.builder().tid(tid).uid(participants.get(LOSER).getUid())
                        .score(LOSER_SCORE)
                        .won(LOSER_SCORE - WINNER_SCORE)
                        .build())
                        .map(Optional::of).collect(toList()),
                Stream.of(WINER, LOSER).map(i ->
                        matchScoreDao.get(adminOpenMatches.get(0).getMid(),
                                participants.get(i).getUid()))
                        .collect(toList()));

        List<TableInfo> tables = tableDao.findTournamentTablesByState(tid, Busy);
        assertEquals(singletonList(Busy), tables.stream().map(TableInfo::getState).collect(toList()));
        assertEquals(singletonList(Optional.of(adminOpenMatches2.get(0).getMid())),
                tables.stream().map(TableInfo::getMid).collect(toList()));
    }

    @Inject
    private Simulator simulator;

    @Test
    public void twoGroupsBy2Guys1TableQuit1() {
        simulator.simulate(T_1_Q_1_G_2,
                TournamentScenario.begin()
                        .category(c1, p1, p2, p3, p4)
                        .win(p1, p2)
                        .lose(p4, p3)
                        .quitsGroup(p1, p3)
                        .win(p1, p3)
                        .champions(c1, p1, p3));
    }

    @Test
    public void aGroupOf2Guys1TableQuit1() {
        simulator.simulate(T_1_Q_1_G_8,
                TournamentScenario.begin()
                        .category(c1, p1, p2)
                        .win(p1, p2)
                        .quitsGroup(p1)
                        .champions(c1, p1));
    }

    @Test
    public void aGroupOf3Guys1TableQuit2() {
        simulator.simulate(T_1_Q_2_G_8,
                TournamentScenario.begin()
                        .category(c1, p1, p2, p3)
                        .win(p1, p2)
                        .win(p1, p3)
                        .lose(p2, p3)
                        .quitsGroup(p1, p3)
                        .lose(p1, p3)
                        .champions(c1, p3, p1));
    }

    @Test
    public void aGroupOf4Guys1TableQuit2_p3_lose_by_sets() {
        simulator.simulate(T_1_Q_2_G_8,
                TournamentScenario.begin()
                        .category(c1, p1, p2, p3, p4)
                        .win(p1, p2) // p1 2 wins
                        .w30(p1, p3)
                        .lose(p1, p4)
                        .win(p4, p2) // p4 2 wins
                        .l23(p4, p3)
                        .win(p3, p2) // p3 1 wins
                        .quitsGroup(p1, p4)
                        .lose(p4, p1)
                        .champions(c1, p1, p4));
    }

    /**
     * 3 guys and max group size 2
     *     => error due not enough people in group
     *          increase max group size and regroup everybody
     * 5 guys and max group size 3, quits 1
     * 5 guys and max group size 3, quits 2
     *
     * group size 3 and 4 groups, 2 quits => 8 base
     * 4 guys in a group 3 of them get same scores.
     * Peak by judge | replay | measure spend time?
     */
}
