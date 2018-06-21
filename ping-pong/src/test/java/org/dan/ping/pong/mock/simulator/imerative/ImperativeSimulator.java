package org.dan.ping.pong.mock.simulator.imerative;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.dan.ping.pong.app.bid.BidResource.BID_RENAME;
import static org.dan.ping.pong.app.bid.BidResource.ENLISTED_BIDS;
import static org.dan.ping.pong.app.category.CategoryResource.CATEGORIES_BY_TID;
import static org.dan.ping.pong.app.category.CategoryResource.CATEGORY_MEMBERS;
import static org.dan.ping.pong.app.group.GroupResource.GROUP_LIST;
import static org.dan.ping.pong.app.group.GroupResource.GROUP_RESULT;
import static org.dan.ping.pong.app.match.AffectedMatchesService.DONT_CHECK_HASH;
import static org.dan.ping.pong.app.match.MatchResource.MATCH_RESULT;
import static org.dan.ping.pong.app.match.MatchResource.OPEN_MATCHES_FOR_JUDGE;
import static org.dan.ping.pong.app.match.MatchResource.RESCORE_MATCH;
import static org.dan.ping.pong.app.match.MatchResource.SCORE_SET;
import static org.dan.ping.pong.app.match.rule.reason.Reason.REASON_CHAIN_TYPE;
import static org.dan.ping.pong.app.tournament.SetScoreResultName.MatchContinues;
import static org.dan.ping.pong.app.tournament.TournamentResource.MY_TOURNAMENT;
import static org.dan.ping.pong.app.tournament.TournamentResource.RESULT_CATEGORY;
import static org.dan.ping.pong.app.tournament.TournamentResource.TOURNAMENT_COPY;
import static org.dan.ping.pong.app.tournament.TournamentResource.TOURNAMENT_EXPEL;
import static org.dan.ping.pong.app.tournament.TournamentResource.TOURNAMENT_INVALIDATE_CACHE;
import static org.dan.ping.pong.app.tournament.TournamentResource.TOURNAMENT_RESIGN;
import static org.dan.ping.pong.app.tournament.TournamentResource.TOURNAMENT_RESULT;
import static org.dan.ping.pong.app.tournament.TournamentState.Close;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Ints;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.BidRename;
import org.dan.ping.pong.app.bid.BidState;
import org.dan.ping.pong.app.bid.ParticipantState;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.category.CategoryInfo;
import org.dan.ping.pong.app.category.CategoryLink;
import org.dan.ping.pong.app.group.GroupInfo;
import org.dan.ping.pong.app.group.GroupParticipants;
import org.dan.ping.pong.app.group.TournamentGroups;
import org.dan.ping.pong.app.match.IdentifiedScore;
import org.dan.ping.pong.app.match.MatchResult;
import org.dan.ping.pong.app.match.MatchState;
import org.dan.ping.pong.app.match.Mid;
import org.dan.ping.pong.app.match.OpenMatchForJudge;
import org.dan.ping.pong.app.match.OpenMatchForJudgeList;
import org.dan.ping.pong.app.match.RescoreMatch;
import org.dan.ping.pong.app.match.SetScoreReq;
import org.dan.ping.pong.app.match.SetScoreResult;
import org.dan.ping.pong.app.tournament.CopyTournament;
import org.dan.ping.pong.app.tournament.ExpelParticipant;
import org.dan.ping.pong.app.tournament.MyTournamentInfo;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.TournamentResultEntry;
import org.dan.ping.pong.app.tournament.TournamentState;
import org.dan.ping.pong.app.user.UserLink;
import org.dan.ping.pong.mock.MyRest;
import org.dan.ping.pong.mock.RestEntityGenerator;
import org.dan.ping.pong.mock.simulator.Player;
import org.dan.ping.pong.mock.simulator.PlayerCategory;
import org.dan.ping.pong.mock.simulator.Simulator;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.dan.ping.pong.sys.ctx.jackson.ObjectMapperProvider;
import org.hamcrest.Matchers;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

@Slf4j
@RequiredArgsConstructor
public class ImperativeSimulator {
    private final Simulator simulator;
    private final RestEntityGenerator restGenerator;
    @Getter
    private final TournamentScenario scenario;
    @Getter
    private TournamentScenario consoleScenario;
    @Getter
    private final MyRest myRest;
    private final Map<Set<Player>, Mid> matchMap = new HashMap<>();
    private boolean matchMapAutoReload;

    private static final ObjectMapper objectMapper = ObjectMapperProvider.get();;

    public ImperativeSimulator autoReload() {
        matchMapAutoReload = true;
        return this;
    }

    public void run(Consumer<ImperativeSimulator> c) {
        try {
            if (scenario.getTid() == null) {
                simulator.setupEnvironment(scenario);
            }
            c.accept(this);
        } catch (AssertionError|Exception e) {
            log.info("Scenario {} failed", scenario, e);
            scenario.getOnFailure().ifPresent(cb -> cb.accept(scenario));
            throw e;
        }
    }

    public ImperativeSimulator beginTournament() {
        restGenerator.beginTournament(scenario.getTestAdmin(), scenario.getTid());
        reloadMatchMap();
        return this;
    }

    public ImperativeSimulator createConsoleTournament() {
        assertNull(consoleScenario);
        scenario.consoleTid(restGenerator.createConsoleTournament(scenario, scenario.getTid()));
        consoleScenario = scenario.createConsoleTournament();
        return this;
    }

    public ImperativeSimulator invalidateTournamentCache() {
        myRest.voidPost(TOURNAMENT_INVALIDATE_CACHE, scenario, scenario.getTid());
        return this;
    }

    public ImperativeSimulator resolveCategories() {
        final int tid = consoleScenario.getTid().getTid();
        final List<CategoryLink> categories = myRest.get(CATEGORIES_BY_TID + tid,
                new GenericType<List<CategoryLink>>(){});

        categories.forEach(category -> {
            final CategoryInfo catInfo = myRest.get(
                    CATEGORY_MEMBERS + tid + "/" + category.getCid(),
                    CategoryInfo.class);
            for (UserLink userLink : catInfo.getUsers()) {
                final Player player = scenario.getUidPlayer().get(userLink.getUid());
                final PlayerCategory catLabel = scenario.getPlayersCategory().get(player);
                consoleScenario.getCategoryDbId().put(catLabel, category.getCid());
                consoleScenario.getPlayersByCategories().put(catLabel, player);
            }
        });

        return new ImperativeSimulator(simulator, restGenerator, consoleScenario, myRest)
                .reloadMatchMap();
    }

    public ImperativeSimulator checkMatchStatus(Player p1, Player p2, MatchState state) {
        Mid mid = resolveMid(p1, p2);
        final MatchResult result = matchResult(mid);
        assertEquals(state, result.getState());
        return this;
    }

    public MatchResult matchResult(Mid mid) {
        return myRest.get(MATCH_RESULT + scenario.getTid().getTid()
                + "/" + mid.intValue(), MatchResult.class);
    }

    public ImperativeSimulator checkTournamentComplete(BidStatesDesc expected) {
        return checkTournament(Close, expected);
    }

    public ImperativeSimulator checkTournament(
            TournamentState expectedTournamentState,
            BidStatesDesc expected) {
        assertEquals(expectedTournamentState, getTournamentInfo().getState());
        final OpenMatchForJudgeList openMatchForJudgeList = openMatches();
        switch (expectedTournamentState) {
            case Canceled:
            case Close:
            case Replaced:
                if (scenario.getConsoleTid().isPresent()) {
                    assertThat(openMatchForJudgeList,
                            hasProperty("matches", not(hasItem(hasProperty("tid", is(scenario.getTid()))))));
                } else {
                    assertThat(openMatchForJudgeList,
                            allOf(
                                    hasProperty("progress",
                                            hasProperty("leftMatches", is(0L))),
                                    hasProperty("matches", empty())));
                }
                break;
            case Open:
                assertThat(openMatchForJudgeList,
                        allOf(
                                hasProperty("progress",
                                        hasProperty("leftMatches", greaterThan(0L))),
                                hasProperty("matches", not(empty()))));
                break;
            case Draft:
                break; // ok
            case Hidden:
                break; // ok
            default:
                throw new IllegalArgumentException("state " + expectedTournamentState);
        }
        checkAllBidsState(expected);
        return this;
    }

    public MyTournamentInfo getTournamentInfo() {
        return myRest.get(MY_TOURNAMENT + tid(),
                MyTournamentInfo.class);
    }

    public List<ParticipantState> enlistedParticipants() {
        return myRest.get(ENLISTED_BIDS + tid(),
                new GenericType<List<ParticipantState>>() {});
    }

    public void checkAllBidsState(BidStatesDesc expectedPattern) {
        final Map<Player, BidState> got = enlistedParticipants().stream()
                .collect(toMap(p -> uid2Player(p.getUser().getUid()),
                        ParticipantState::getState));
        final Map<Player, BidState> expected = got.keySet().stream().collect(
                toMap(o -> o, o -> expectedPattern.getRest()));
        expected.putAll(expectedPattern.getSpecial());
        assertThat(got, is(expected));
    }

    public OpenMatchForJudgeList openMatches() {
        return myRest.get(OPEN_MATCHES_FOR_JUDGE + tid(),
                OpenMatchForJudgeList.class);
    }

    private int tid() {
        return scenario.getTid().getTid();
    }

    public TournamentGroups tournamentGroups() {
        return myRest.get(GROUP_LIST + tid(), TournamentGroups.class);
    }

    private Player uid2Player(Uid uid) {
        return ofNullable(scenario.getUidPlayer().get(uid))
                .orElseThrow(
                        () -> new RuntimeException(
                                "no player label for uid " + uid));
    }

    public ImperativeSimulator playerQuits(Player p) {
        myRest.voidPost(TOURNAMENT_RESIGN,
                ofNullable(scenario.getPlayersSessions().get(p))
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "no session for player " + p)),
                scenario.getTid());
        reloadMatchMap();
        return this;
    }

    public ImperativeSimulator expelPlayer(Player p) {
        return expelPlayer(p, BidState.Expl);
    }

    public ImperativeSimulator expelPlayer(Player p, BidState targetState) {
        myRest.voidPost(TOURNAMENT_EXPEL,
                scenario.getTestAdmin(),
                ExpelParticipant.builder()
                        .tid(scenario.getTid())
                        .uid(scenario.player2Uid(p))
                        .targetBidState(targetState)
                        .build());
        reloadMatchMap();
        return this;
    }

    private ImperativeSimulator doAutoReload() {
        if (matchMapAutoReload) {
            reloadMatchMap();
        }
        return this;
    }

    public ImperativeSimulator checkResult(Player... p) {
        return checkResult(getOnlyCid(), singletonList(asList(p)));
    }

    public ImperativeSimulator checkResult(List<Player>... p) {
        return checkResult(getOnlyCid(), asList(p));
    }

    public ImperativeSimulator checkResult(PlayerCategory category, List<List<Player>> p) {
        return checkResult(ofNullable(getScenario().getCategoryDbId()
                .get(category)).orElseThrow(() -> new RuntimeException("no category " + category)), p);
    }

    @SneakyThrows
    public ImperativeSimulator checkResult(int categoryId, List<List<Player>> p) {
        final List<TournamentResultEntry> tournamentResult = getTournamentResult(categoryId, tid());
        try {
            assertThat(tournamentResult.stream()
                            .map(tr -> uid2Player(tr.getUser().getUid())).collect(toList()),
                    anyOf(p.stream().map(Matchers::is).collect(toList())));
        } catch (AssertionError e) {
            log.error("failed tournament results: {}", tournamentResult);
            throw new AssertionError(
                    "failed tournament results: "
                            + objectMapper.writerFor(REASON_CHAIN_TYPE)
                            .writeValueAsString(tournamentResult),
                    e);
        }
        return this;
    }

    public List<TournamentResultEntry> getTournamentResult() {
        return getTournamentResult(getOnlyCid(), tid());
    }

    public ImperativeSimulator checkPlayOffLevels(int... levels) {
        return checkPlayOffLevels(getTournamentResult(), levels);
    }

    public ImperativeSimulator checkPlayOffLevels(List<TournamentResultEntry> tournamentResult , int... levels) {
        final List<Optional<Integer>> expectedLevels = Ints.asList(levels).stream()
                .map((Integer l) -> (l < 0) ? Optional.<Integer>empty() : Optional.of(l))
                .collect(toList());

        assertEquals(expectedLevels, tournamentResult.stream()
                .map(TournamentResultEntry::getPlayOffStep).collect(toList()));
        return this;
    }

    public GroupParticipants getGroupResult(int gid) {
        return myRest.get(GROUP_RESULT + tid() + "/" + gid,
                GroupParticipants.class);
    }

    public int gidOf(int cid, int groupIndex) {
        final TournamentGroups tGroups = tournamentGroups();
        return tGroups.getGroups().stream()
                .filter(g -> g.getCid() == cid)
                .skip(groupIndex)
                .map(GroupInfo::getGid)
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "num of groups is less than "
                        + groupIndex));
    }

    public List<TournamentResultEntry> getTournamentResult(Integer onlyCid, int tid) {
        return myRest.get(TOURNAMENT_RESULT + tid
                        + RESULT_CATEGORY + onlyCid,
                new GenericType<List<TournamentResultEntry>>() {});

    }

    private Integer getOnlyCid() {
        if (scenario.getCategoryDbId().size() > 1) {
            throw new RuntimeException("more than 1 category");
        }
        return scenario.getCategoryDbId().values().iterator().next();
    }

    public ImperativeSimulator rescoreMatch3(Player p1, Player p2, int games1, int games2) {
        return rescoreMatch(p1, p2, games1, games2, games1, games2, games1, games2);
    }

    public ImperativeSimulator rescoreMatch(Player p1, Player p2, int... games) {
        checkArgument(games.length % 2 == 0);
        List<Integer> games1 = IntStream.range(0, games.length)
                .filter(i -> (i % 2) == 0)
                .mapToObj(i -> games[i])
                .collect(toList());
        List<Integer> games2 = IntStream.range(0, games.length)
                .filter(i -> (i % 2) == 1)
                .mapToObj(i -> games[i])
                .collect(toList());
        myRest.voidPost(RESCORE_MATCH, scenario.getTestAdmin(),
                RescoreMatch.builder()
                        .tid(scenario.getTid())
                        .effectHash(Optional.of(DONT_CHECK_HASH))
                        .mid(resolveMid(p1, p2))
                        .sets(ImmutableMap.of(
                                scenario.player2Uid(p1),
                                games1,
                                scenario.player2Uid(p2),
                                games2))
                        .build());
        return doAutoReload();
    }

    public ImperativeSimulator scoreSet(Player p1, int games1, Player p2, int games2) {
        return scoreSet(0, p1, games1, p2, games2);
    }

    public ImperativeSimulator scoreSet2(Player p1, int games1, Player p2, int games2) {
        return scoreSet(0, p1, games1, p2, games2)
                .scoreSet(1, p1, games1, p2, games2);
    }

    public ImperativeSimulator and(Function<ImperativeSimulator, ImperativeSimulator> f) {
        return f.apply(this);
    }

    public ImperativeSimulator storeMatchMap(Map<Set<Player>, Mid> memory) {
        memory.putAll(matchMap);
        return this;
    }

    public ImperativeSimulator restoreMatchMap(Map<Set<Player>, Mid> memory) {
        matchMap.putAll(memory);
        return this;
    }

    public ImperativeSimulator scoreSet3(Player p1, int games1, Player p2, int games2) {
        return scoreSet3(0, p1, games1, p2, games2);
    }

    public ImperativeSimulator scoreSet3(int n, Player p1, int games1, Player p2, int games2) {
        return scoreSet(n, p1, games1, p2, games2)
                .scoreSet(n + 1, p1, games1, p2, games2)
                .scoreSet(n + 2, p1, games1, p2, games2);
    }

    public ImperativeSimulator scoreSetLast2(Player p1, int games1, Player p2, int games2) {
        return scoreSet(1, p1, games1, p2, games2)
                .scoreSet(2, p1, games1, p2, games2);
    }

    public ImperativeSimulator scoreSet(int set, Player p1, int games1, Player p2, int games2) {
        final Response response = myRest.post(SCORE_SET, scenario.getTestAdmin(),
                SetScoreReq.builder()
                        .tid(scenario.getTid())
                        .mid(resolveMid(p1, p2))
                        .setOrdNumber(set)
                        .scores(asList(identifiedScore(p1, games1),
                                identifiedScore(p2, games2)))
                        .build());
        final SetScoreResult setScoreResult = response.readEntity(SetScoreResult.class);
        if (setScoreResult.getScoreOutcome() == MatchContinues) {
            return this;
        }
        return doAutoReload();
    }

    private IdentifiedScore identifiedScore(Player p1, int games1) {
        return IdentifiedScore.builder()
                .score(games1)
                .uid(scenario.player2Uid(p1))
                .build();
    }

    public Mid resolveMid(Player p1, Player p2) {
        final ImmutableSet<Player> players = ImmutableSet.of(p1, p2);
        {
            final Mid mid = matchMap.get(players);
            if (mid == null) {
                reloadMatchMap();
            } else {
                return mid;
            }
        }
        {
            final Mid mid = matchMap.get(players);
            if (mid == null) {
                throw new RuntimeException(
                        "Match between " + p1 + " and "
                                + p2 + " does not exist");
            }
            return mid;
        }
    }

    public ImperativeSimulator reloadMatchMap() {
        matchMap.putAll(openMatches().getMatches().stream().collect(toMap(
                m -> m.getParticipants().stream()
                        .map(
                                pl -> uid2Player(pl.getUid()))
                        .collect(toSet()),
                OpenMatchForJudge::getMid)));
        return this;
    }

    public Tid copyTournament(String name, Instant opensAt) {
        return myRest.post(TOURNAMENT_COPY, scenario.getTestAdmin(),
                CopyTournament.builder()
                        .name(name)
                        .opensAt(opensAt)
                        .originTid(scenario.getTid())
                        .build())
                .readEntity(Tid.class);
    }

    public ImperativeSimulator renameBid(Player p, String newName) {
        final Uid uid = scenario.player2Uid(p);
        myRest.post(BID_RENAME, scenario.getTestAdmin(),
                BidRename
                        .builder()
                        .expectedName(scenario.getPlayersSessions().get(p).getName())
                        .tid(scenario.getTid())
                        .newName(newName)
                        .uid(uid)
                        .build());
        return this;
    }
}
