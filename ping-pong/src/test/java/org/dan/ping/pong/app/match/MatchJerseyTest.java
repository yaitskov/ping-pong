package org.dan.ping.pong.app.match;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.dan.ping.pong.app.bid.BidState.Lost;
import static org.dan.ping.pong.app.bid.BidState.Win1;
import static org.dan.ping.pong.app.castinglots.MatchScheduleInGroupJerseyTest.G2Q1;
import static org.dan.ping.pong.app.castinglots.MatchScheduleInGroupJerseyTest.G8Q1;
import static org.dan.ping.pong.app.castinglots.MatchScheduleInGroupJerseyTest.G8Q2;
import static org.dan.ping.pong.app.castinglots.MatchScheduleInGroupJerseyTest.S1A2G11;
import static org.dan.ping.pong.app.castinglots.MatchScheduleInGroupJerseyTest.S3A2G11;
import static org.dan.ping.pong.app.match.MatchResource.MATCH_WATCH_LIST_OPEN;
import static org.dan.ping.pong.app.match.MatchResource.SCORE_SET;
import static org.dan.ping.pong.app.match.MatchState.Over;
import static org.dan.ping.pong.app.match.MatchType.Grup;
import static org.dan.ping.pong.app.playoff.PlayOffRule.L1_3P;
import static org.dan.ping.pong.app.playoff.PlayOffRule.Losing1;
import static org.dan.ping.pong.app.table.TableState.Free;
import static org.dan.ping.pong.app.tournament.TournamentState.Close;
import static org.dan.ping.pong.mock.AdminSessionGenerator.ADMIN_SESSION;
import static org.dan.ping.pong.mock.DaoEntityGeneratorWithAdmin.INCREASE_PROVIDED_RANKING;
import static org.dan.ping.pong.mock.DaoEntityGeneratorWithAdmin.INCREASE_SIGNUP_CASTING;
import static org.dan.ping.pong.mock.DaoEntityGeneratorWithAdmin.INCREASE_SIGNUP_MIX;
import static org.dan.ping.pong.mock.simulator.FixedSetGenerator.game;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.Player.p4;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.bid.BidDao;
import org.dan.ping.pong.app.place.ArenaDistributionPolicy;
import org.dan.ping.pong.app.place.ForTestPlaceDao;
import org.dan.ping.pong.app.place.Pid;
import org.dan.ping.pong.app.place.PlaceRules;
import org.dan.ping.pong.app.score.MatchScoreDao;
import org.dan.ping.pong.app.table.TableInfo;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.TournamentDao;
import org.dan.ping.pong.app.tournament.TournamentRow;
import org.dan.ping.pong.app.tournament.TournamentRules;
import org.dan.ping.pong.app.user.UserLink;
import org.dan.ping.pong.mock.DaoEntityGeneratorWithAdmin;
import org.dan.ping.pong.mock.MyLocalRest;
import org.dan.ping.pong.mock.RestEntityGeneratorWithAdmin;
import org.dan.ping.pong.mock.TestAdmin;
import org.dan.ping.pong.mock.TestUserSession;
import org.dan.ping.pong.mock.TournamentProps;
import org.dan.ping.pong.mock.UserSessionGenerator;
import org.dan.ping.pong.mock.simulator.FixedSetGenerator;
import org.dan.ping.pong.mock.simulator.Hook;
import org.dan.ping.pong.mock.simulator.HookDecision;
import org.dan.ping.pong.mock.simulator.PlayHook;
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
import javax.ws.rs.core.Response;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = {TestCtx.class, ForTestMatchDao.class,
        ForTestBidDao.class, ForTestPlaceDao.class, Simulator.class})
public class MatchJerseyTest extends AbstractSpringJerseyTest {
    private static final int LOSER = 0;
    private static final int WINER = 1;

    private static final int WINNER_SCORE = 11;
    private static final int LOSER_SCORE = 1;
    public static final PlaceRules GLOBAL = PlaceRules.builder()
            .arenaDistribution(ArenaDistributionPolicy.GLOBAL).build();
    public static final TournamentRules RULES_G8Q1_S3A2G11 = TournamentRules
            .builder()
            .match(S3A2G11)
            .group(Optional.of(G8Q1))
            .casting(INCREASE_SIGNUP_CASTING)
            .playOff(Optional.of(Losing1))
            .place(Optional.of(GLOBAL))
            .build();

    public static final TournamentRules RULES_G_S3A2G11 = RULES_G8Q1_S3A2G11.withPlayOff(Optional.empty());

    public static final TournamentRules RULES_G8Q1_S1A2G11 = TournamentRules
            .builder()
            .match(S1A2G11)
            .group(Optional.of(G8Q1))
            .casting(INCREASE_SIGNUP_CASTING)
            .playOff(Optional.of(Losing1))
            .place(Optional.of(GLOBAL))
            .build();

    public static final TournamentRules RULES_G2Q1_S1A2G11_PRNK = TournamentRules
            .builder()
            .match(S1A2G11)
            .group(Optional.of(G2Q1))
            .casting(INCREASE_PROVIDED_RANKING)
            .playOff(Optional.of(Losing1))
            .place(Optional.of(GLOBAL))
            .build();

    public static final TournamentRules RULES_G2Q1_S1A2G11 = TournamentRules
            .builder()
            .match(S1A2G11)
            .group(Optional.of(G2Q1))
            .casting(INCREASE_SIGNUP_CASTING)
            .playOff(Optional.of(Losing1))
            .place(Optional.of(GLOBAL))
            .build();

    public static final TournamentRules RULES_G_S1A2G11 = RULES_G8Q1_S1A2G11.withPlayOff(Optional.empty());
    public static final TournamentRules RULES_G_S1A2G11_NP = RULES_G_S1A2G11.withPlace(Optional.empty());

    public static final TournamentRules RULES_G2Q1_S1A2G11_MIX = TournamentRules
            .builder()
            .match(S1A2G11)
            .group(Optional.of(G2Q1))
            .casting(INCREASE_SIGNUP_MIX)
            .playOff(Optional.of(Losing1))
            .place(Optional.of(GLOBAL))
            .build();

    public static final TournamentRules RULES_G2Q1_S1A2G11_3P = TournamentRules
            .builder()
            .match(S1A2G11)
            .group(Optional.of(G2Q1))
            .casting(INCREASE_SIGNUP_CASTING)
            .playOff(Optional.of(L1_3P))
            .place(Optional.of(GLOBAL))
            .build();

    public static final TournamentRules RULES_G8Q2_S1A2G11 = TournamentRules
            .builder()
            .match(S1A2G11)
            .group(Optional.of(G8Q2))
            .casting(INCREASE_SIGNUP_CASTING)
            .playOff(Optional.of(Losing1))
            .place(Optional.of(GLOBAL))
            .build();

    public static final TournamentRules RULES_G8Q2_S3A2G11 = TournamentRules
            .builder()
            .match(S3A2G11)
            .group(Optional.of(G8Q2))
            .casting(INCREASE_SIGNUP_CASTING)
            .playOff(Optional.of(Losing1))
            .place(Optional.of(GLOBAL))
            .build();

    public static final TournamentRules RULES_G2Q1_S3A2G11 = TournamentRules
            .builder()
            .match(S3A2G11)
            .group(Optional.of(G2Q1))
            .casting(INCREASE_SIGNUP_CASTING)
            .playOff(Optional.of(Losing1))
            .place(Optional.of(GLOBAL))
            .build();

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
    private ForTestMatchDao forTestMatchDao;

    @Inject
    private ForTestBidDao forTestBidDao;

    @Inject
    private TournamentDao tournamentDao;

    @Inject
    private ForTestPlaceDao placeDao;

    @Test
    public void adminCompleteOnlyMatchIn1Group() {
        final Pid placeId = daoGenerator.genPlace(1);
        final Tid tid = daoGenerator.genTournament(placeId,
                TournamentProps.builder().rules(RULES_G8Q1_S1A2G11).build());
        final int cid = daoGenerator.genCategory(tid);
        final List<TestUserSession> participants = userSessionGenerator.generateUserSessions(2);
        restGenerator.enlistParticipants(tid, cid, participants);
        assertEquals(emptyList(), restGenerator.listOpenMatches(tid).getMatches());
        restGenerator.beginTournament(tid);

        final List<OpenMatchForJudge> adminOpenMatches = restGenerator.listOpenMatches(tid).getMatches();

        assertEquals(
                participants.stream().map(TestUserSession::getUid).collect(toSet()),
                adminOpenMatches.stream().map(OpenMatchForJudge::getParticipants)
                        .flatMap(List::stream)
                        .map(UserLink::getUid)
                        .collect(toSet()));

        rest.voidPost(SCORE_SET, adminSession,
                SetScoreReq.builder()
                        .tid(tid)
                        .mid(adminOpenMatches.get(0).getMid())
                        .scores(asList(
                                IdentifiedScore.builder().score(LOSER_SCORE)
                                        .uid(participants.get(LOSER).getUid()).build(),
                                IdentifiedScore.builder().score(WINNER_SCORE)
                                        .uid(participants.get(WINER).getUid()).build()))
                        .build());

        assertEquals(Stream.of(Win1, Lost).map(Optional::of).collect(toList()),
                asList(forTestBidDao.getState(tid, participants.get(WINER).getUid()),
                        forTestBidDao.getState(tid, participants.get(LOSER).getUid())));

        assertEquals(Stream.of(Over, Close).map(Optional::of).collect(toList()),
                asList(forTestMatchDao.getById(adminOpenMatches.get(0).getMid())
                                .map(MatchInfo::getState),
                        tournamentDao.getRow(tid).map(TournamentRow::getState)));

        List<TableInfo> tables = placeDao.findFreeTables(tid);
        assertEquals(singletonList(Free), tables.stream().map(TableInfo::getState).collect(toList()));
        assertEquals(singletonList(Optional.empty()),
                tables.stream().map(TableInfo::getMid).collect(toList()));
        assertEquals(emptyList(), restGenerator.listOpenMatches(tid).getMatches());
    }

    @Test
    public void listOpenMatchesForWatch() {
        TournamentScenario scenario = TournamentScenario.begin()
                .category(c1, p1, p2)
                .rules(RULES_G8Q1_S1A2G11)
                .ignoreUnexpectedGames();

        simulator.simulate(scenario);
        List<OpenMatchForWatch> result = rest.get(MATCH_WATCH_LIST_OPEN + scenario.getTid().getTid(),
                new GenericType<List<OpenMatchForWatch>>() {});
        assertThat(result, hasItem(allOf(
                hasProperty("mid", hasProperty("id", greaterThan(0))),
                hasProperty("type", is(Grup)),
                hasProperty("score", is(asList(0, 0))),
                hasProperty("category", allOf(
                        hasProperty("name", notNullValue()),
                        hasProperty("cid", is(scenario.getCategoryDbId().get(c1))))),
                hasProperty("participants", hasItems(
                        allOf(
                                hasProperty("name", notNullValue()),
                                hasProperty("uid", is(scenario.getPlayersSessions()
                                        .get(p1).getUid()))),
                        allOf(
                                hasProperty("name", notNullValue()),
                                hasProperty("uid", is(scenario.getPlayersSessions()
                                        .get(p2).getUid()))))))));
    }

    @Inject
    private Simulator simulator;

    @Test
    public void twoGroupsBy2Guys1TableQuit1() {
        simulator.simulate(
                TournamentScenario.begin()
                        .rules(RULES_G2Q1_S1A2G11)
                        .category(c1, p1, p2, p3, p4)
                        .win(p1, p2)
                        .lose(p4, p3)
                        .quitsGroup(p1, p3)
                        .win(p1, p3)
                        .champions(c1, p1, p3));
    }

    @Test
    public void aGroupOf2Guys1TableQuit1() {
        simulator.simulate(
                TournamentScenario.begin()
                        .rules(RULES_G8Q1_S1A2G11)
                        .category(c1, p1, p2)
                        .win(p1, p2)
                        .quitsGroup(p1)
                        .champions(c1, p1));
    }

    @Test
    public void aGroupOf3Guys1TableQuit2() {
        simulator.simulate(
                TournamentScenario.begin()
                        .rules(RULES_G8Q2_S1A2G11)
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
        simulator.simulate(
                TournamentScenario.begin()
                        .rules(RULES_G8Q2_S3A2G11)
                        .category(c1, p1, p2, p3, p4)
                        .w31(p1, p2) // p1 2 wins
                        .w30(p1, p3)
                        .l13(p1, p4)
                        .w31(p4, p2) // p4 2 wins
                        .l23(p4, p3)
                        .w31(p3, p2) // p3 1 wins
                        .quitsGroup(p1, p4)
                        .lose(p4, p1)
                        .champions(c1, p1, p4));
    }

    @Test
    public void acceptScoreIfItTheSame() {
        final FixedSetGenerator game = game(p1, p2, 11, 2, 11, 3, 11, 5);
        TournamentScenario scenario = TournamentScenario.begin()
                .rules(RULES_G8Q1_S3A2G11)
                .category(c1, p1, p2)
                .custom(game)
                .quitsGroup(p1)
                .pause(p1, p2, PlayHook.builder()
                        .type(Hook.AfterScore)
                        .callback((s, meta) -> {
                            myRest().voidPost(SCORE_SET, s.getPlayersSessions().get(p1),
                                    SetScoreReq.builder()
                                            .tid(s.getTid())
                                            .mid(meta.getOpenMatch().getMid())
                                            .scores(asList(
                                                    IdentifiedScore.builder()
                                                            .score(s.getRules().getMatch().getMinGamesToWin())
                                                            .uid(s.getPlayersSessions().get(p1).getUid())
                                                            .build(),
                                                    IdentifiedScore.builder()
                                                            .score(2)
                                                            .uid(s.getPlayersSessions().get(p2).getUid())
                                                            .build()))
                                            .build());
                            return HookDecision.Skip;
                        })
                        .build())
                .champions(c1, p1)
                .ignoreUnexpectedGames();

        simulator.simulate(scenario);
    }

    @Test
    public void rejectDifferentScore() {
        final FixedSetGenerator game = game(p1, p2, 11, 2, 11, 3, 11, 5);
        TournamentScenario scenario = TournamentScenario.begin()
                .rules(RULES_G8Q1_S3A2G11)
                .category(c1, p1, p2)
                .custom(game)
                .quitsGroup(p1)
                .pause(p1, p2, PlayHook.builder()
                        .type(Hook.AfterScore)
                        .callback((s, meta) -> {
                            final Response re = myRest().post(SCORE_SET, s.getPlayersSessions().get(p1),
                                    SetScoreReq.builder()
                                            .setOrdNumber(0)
                                            .tid(s.getTid())
                                            .mid(meta.getOpenMatch().getMid())
                                            .scores(asList(
                                                    IdentifiedScore.builder()
                                                            .score(s.getRules().getMatch().getMinGamesToWin())
                                                            .uid(s.getPlayersSessions().get(p1).getUid())
                                                            .build(),
                                                    IdentifiedScore.builder()
                                                            .score(3)
                                                            .uid(s.getPlayersSessions().get(p2).getUid())
                                                            .build()))
                                            .build());
                            assertEquals(400, re.getStatus());
                            assertEquals("Match is already scored",
                                    re.readEntity(MatchScoredError.class).getMessage());
                            return HookDecision.Skip;
                        })
                        .build())
                .champions(c1, p1)
                .ignoreUnexpectedGames();

        simulator.simulate(scenario);
    }

    @Test
    public void participantPlays1GameAtTime() {
        simulator.simulate(
                TournamentScenario.begin()
                        .tables(3)
                        .rules(RULES_G8Q1_S1A2G11)
                        .category(c1, p1, p2, p3)
                        .win(p1, p2)
                        .win(p1, p3)
                        .lose(p2, p3)
                        .quitsGroup(p1)
                        .champions(c1, p1));
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
