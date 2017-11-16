package org.dan.ping.pong.mock.simulator;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.dan.ping.pong.app.match.MatchEditorService.DONT_CHECK_HASH;
import static org.dan.ping.pong.app.match.MatchResource.RESCORE_MATCH;
import static org.dan.ping.pong.app.match.MatchResource.OPEN_MATCHES_FOR_JUDGE;
import static org.dan.ping.pong.app.match.MatchResource.SCORE_SET;
import static org.dan.ping.pong.app.tournament.TournamentResource.MY_TOURNAMENT;
import static org.dan.ping.pong.app.tournament.TournamentResource.RESULT_CATEGORY;
import static org.dan.ping.pong.app.tournament.TournamentResource.TOURNAMENT_EXPEL;
import static org.dan.ping.pong.app.tournament.TournamentResource.TOURNAMENT_RESIGN;
import static org.dan.ping.pong.app.tournament.TournamentResource.TOURNAMENT_RESULT;
import static org.dan.ping.pong.app.tournament.TournamentState.Close;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.match.IdentifiedScore;
import org.dan.ping.pong.app.match.Mid;
import org.dan.ping.pong.app.match.OpenMatchForJudge;
import org.dan.ping.pong.app.match.OpenMatchForJudgeList;
import org.dan.ping.pong.app.match.RescoreMatch;
import org.dan.ping.pong.app.match.SetScoreReq;
import org.dan.ping.pong.app.tournament.ExpelParticipant;
import org.dan.ping.pong.app.tournament.MyTournamentInfo;
import org.dan.ping.pong.app.tournament.TournamentResultEntry;
import org.dan.ping.pong.mock.MyRest;
import org.dan.ping.pong.mock.RestEntityGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import javax.ws.rs.core.GenericType;

@Slf4j
@RequiredArgsConstructor
public class ImperativeSimulator {
    private final Simulator simulator;
    private final RestEntityGenerator restGenerator;
    private final TournamentScenario scenario;
    private final MyRest myRest;
    private final Map<Set<Player>, Mid> matchMap = new HashMap<>();

    public void run(Consumer<ImperativeSimulator> c) {
        try {
            simulator.setupEnvironment(scenario);
            c.accept(this);
        } catch (AssertionError|Exception e) {
            log.info("Scenario {} failed", scenario, e);
            scenario.getOnFailure().ifPresent(cb -> cb.accept(scenario));
            throw e;
        }
    }

    public void beginTournament() {
        restGenerator.beginTournament(scenario.getTestAdmin(), scenario.getTid());
    }

    public void checkTournamentComplete() {
        assertEquals(Close, getTournamentInfo().getState());
    }

    public MyTournamentInfo getTournamentInfo() {
        return myRest.get(MY_TOURNAMENT + scenario.getTid().getTid(),
                MyTournamentInfo.class);
    }

    public OpenMatchForJudgeList openMatches() {
        return myRest.get(OPEN_MATCHES_FOR_JUDGE + scenario.getTid().getTid(),
                OpenMatchForJudgeList.class);
    }

    private Player uid2Player(Uid uid) {
        return ofNullable(scenario.getUidPlayer().get(uid))
                .orElseThrow(
                        () -> new RuntimeException(
                                "no player label for uid " + uid));
    }

    public void playerQuits(Player p) {
        myRest.voidPost(TOURNAMENT_RESIGN,
                ofNullable(scenario.getPlayersSessions().get(p))
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "no session for player " + p)),
                scenario.getTid());
    }

    public void expelPlayer(Player p) {
        myRest.voidPost(TOURNAMENT_EXPEL,
                scenario.getTestAdmin(),
                ExpelParticipant.builder()
                        .tid(scenario.getTid())
                        .uid(scenario.player2Uid(p))
                        .build());
    }

    public void checkResult(Player... p) {
        assertThat(getTournamentResult().stream()
                        .map(tr -> uid2Player(tr.getUser().getUid())).collect(toList()),
                is(asList(p)));
    }

    public List<TournamentResultEntry> getTournamentResult() {
        return getTournamentResult(getOnlyCid());
    }

    public List<TournamentResultEntry> getTournamentResult(Integer onlyCid) {
        return myRest.get(TOURNAMENT_RESULT + scenario.getTid().getTid()
                        + RESULT_CATEGORY + onlyCid,
                new GenericType<List<TournamentResultEntry>>() {});

    }

    private Integer getOnlyCid() {
        if (scenario.getCategoryDbId().size() > 1) {
            throw new RuntimeException("more than 1 category");
        }
        return scenario.getCategoryDbId().values().iterator().next();
    }

    public void rescoreMatch(Player p1, Player p2, int... games) {
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
                        .effectHash(DONT_CHECK_HASH)
                        .mid(resolveMid(p1, p2))
                        .sets(ImmutableMap.of(
                                scenario.player2Uid(p1),
                                games1,
                                scenario.player2Uid(p2),
                                games2))
                        .build());
    }

    public void scoreSet(int set, Player p1, int games1, Player p2, int games2) {
        myRest.voidPost(SCORE_SET, scenario.getTestAdmin(),
                SetScoreReq.builder()
                        .tid(scenario.getTid())
                        .mid(resolveMid(p1, p2))
                        .setOrdNumber(set)
                        .scores(asList(identifiedScore(p1, games1),
                                identifiedScore(p2, games2)))
                        .build());
    }

    private IdentifiedScore identifiedScore(Player p1, int games1) {
        return IdentifiedScore.builder()
                .score(games1)
                .uid(scenario.player2Uid(p1))
                .build();
    }

    private Mid resolveMid(Player p1, Player p2) {
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

    public void reloadMatchMap() {
        matchMap.putAll(openMatches().getMatches().stream().collect(toMap(
                m -> m.getParticipants().stream()
                        .map(
                                pl -> uid2Player(pl.getUid()))
                        .collect(toSet()),
                OpenMatchForJudge::getMid)));
    }
}
