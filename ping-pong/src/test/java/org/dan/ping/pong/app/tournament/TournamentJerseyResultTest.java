package org.dan.ping.pong.app.tournament;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.dan.ping.pong.app.bid.BidState.Expl;
import static org.dan.ping.pong.app.bid.BidState.Lost;
import static org.dan.ping.pong.app.bid.BidState.Play;
import static org.dan.ping.pong.app.group.GroupResource.GROUP_LIST;
import static org.dan.ping.pong.app.group.GroupResource.GROUP_RESULT;
import static org.dan.ping.pong.app.match.GroupScoreJerseyTest.RULES_G8Q2_S1A2G11_M;
import static org.dan.ping.pong.app.tournament.TournamentRulesConst.RULES_G3Q2_S1A2G11;
import static org.dan.ping.pong.app.tournament.TournamentRulesConst.RULES_G8Q2_S3A2G11;
import static org.dan.ping.pong.app.tournament.TournamentRulesConst.RULES_JP_S1A2G11;
import static org.dan.ping.pong.app.playoff.PlayOffRule.Losing1;
import static org.dan.ping.pong.app.tournament.TournamentResource.RESULT_CATEGORY;
import static org.dan.ping.pong.app.tournament.TournamentResource.TOURNAMENT_RESULT;
import static org.dan.ping.pong.app.tournament.TournamentState.Open;
import static org.dan.ping.pong.mock.simulator.FixedSetGenerator.game;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.Player.p4;
import static org.dan.ping.pong.mock.simulator.Player.p5;
import static org.dan.ping.pong.mock.simulator.Player.p6;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c2;
import static org.dan.ping.pong.mock.simulator.TournamentScenario.begin;
import static org.dan.ping.pong.mock.simulator.imerative.BidStatesDesc.restState;
import static org.junit.Assert.assertEquals;

import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.group.GroupParticipantResult;
import org.dan.ping.pong.app.group.GroupParticipants;
import org.dan.ping.pong.app.group.TournamentGroups;
import org.dan.ping.pong.mock.simulator.Simulator;
import org.dan.ping.pong.mock.simulator.TournamentScenario;
import org.dan.ping.pong.mock.simulator.imerative.ImperativeSimulator;
import org.dan.ping.pong.mock.simulator.imerative.ImperativeSimulatorFactory;
import org.dan.ping.pong.test.AbstractSpringJerseyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.core.GenericType;

@Category(JerseySpringTest.class)
@ContextConfiguration(classes = JerseyWithSimulator.class)
public class TournamentJerseyResultTest extends AbstractSpringJerseyTest {
    @Inject
    private Simulator simulator;

    @Inject
    private TournamentService tournamentService;

    @Inject
    private ImperativeSimulatorFactory isf;

    @Test
    public void tournamentResult() {
        final TournamentScenario scenario = begin()
                .name("tournamentResult")
                .rules(RULES_G8Q2_S3A2G11)
                .category(c1, p1, p2, p3)
                .category(c2, p4, p5, p6)
                .w31(p1, p2)
                .w30(p1, p3)
                .w32(p2, p3)
                .quitsGroup(p1, p2)
                .w31(p1, p2)
                .champions(c1, p1, p2)
                .w31(p4, p5)
                .w30(p4, p6)
                .w32(p5, p6)
                .quitsGroup(p4, p5)
                .w32(p4, p5)
                .champions(c2, p4, p5);

        simulator.simulate(scenario);
        final List<TournamentResultEntry> result = result(scenario);

        isf.resume(scenario).run(c -> c.checkPlayOffLevels(result, 1, 1, -1));

        TournamentResultEntry p1Result = result.get(0);
        assertEquals(scenario.getPlayersSessions().get(p1).getUid(),
                p1Result.getUser().getUid());

        TournamentResultEntry p2Result = result.get(1);
        assertEquals(scenario.getPlayersSessions().get(p2).getUid(),
                p2Result.getUser().getUid());

        TournamentResultEntry p3Result = result.get(2);
        assertEquals(scenario.getPlayersSessions().get(p3).getUid(),
                p3Result.getUser().getUid());
    }

    @Test
    public void winMinusLose() {
        final TournamentScenario scenario = begin()
                .name("ResultWinMinusLose")
                .rules(RULES_G8Q2_S1A2G11_M)
                .category(c1, p1, p2, p3)
                .custom(game(p1, p2, 11, 0))
                .custom(game(p2, p3, 11, 1))
                .custom(game(p3, p1, 14, 12))
                .quitsGroup(p1, p2)
                .champions(c1, p1, p2);

        simulator.simulate(scenario);

        final List<TournamentResultEntry> result = result(scenario);

        assertEquals(asList(p1, p2, p3),
                result.stream()
                        .map(e -> scenario.getUidPlayer()
                                .get(e.getUser().getUid()))
                        .collect(toList()));

        isf.resume(scenario).run(c -> c.checkPlayOffLevels(result, -1, -1, -1));

        final int tid = scenario.getTid().getTid();
        final TournamentGroups g = myRest().get(GROUP_LIST + tid, TournamentGroups.class);
        final int gid = g.getGroups().stream().findFirst().get().getGid();
        final GroupParticipants r = myRest().get(GROUP_RESULT + tid + "/"
                + gid, GroupParticipants.class);

        assertEquals(asList(p1, p2, p3),
                r.getParticipants().stream()
                        .sorted(Comparator.comparingInt(GroupParticipantResult::getFinishPosition))
                        .map(GroupParticipantResult::getUid)
                        .map(e -> scenario.getUidPlayer().get(e))
                        .collect(toList()));
    }

    private List<TournamentResultEntry> result(TournamentScenario scenario) {
        return myRest()
                .get(TOURNAMENT_RESULT + scenario.getTid() + RESULT_CATEGORY + scenario.getCategoryDbId().get(c1),
                        new GenericType<List<TournamentResultEntry>>() {
                        });
    }

    @Test
    public void playOffIncomplete() {
        final TournamentScenario scenario = begin()
                .name("ResultPlayOffIncomplete")
                .ignoreUnexpectedGames()
                .rules(RULES_G8Q2_S1A2G11_M.withPlayOff(Optional.of(Losing1)))
                .category(c1, p1, p2, p3)
                .custom(game(p1, p2, 11, 1))
                .custom(game(p2, p3, 11, 0))
                .custom(game(p3, p1, 14, 12));

        simulator.simulate(scenario);

        final List<TournamentResultEntry> result = result(scenario);

        assertEquals(asList(p1, p2, p3),
                result.stream()
                        .map(e -> scenario.getUidPlayer()
                                .get(e.getUser().getUid()))
                        .collect(toList()));

        isf.resume(scenario).run(c -> c.checkPlayOffLevels(result, 1, 1, -1));

        final int tid = scenario.getTid().getTid();
        final TournamentGroups g = myRest().get(GROUP_LIST + tid, TournamentGroups.class);
        final int gid = g.getGroups().stream().findFirst().get().getGid();
        final GroupParticipants r = myRest().get(GROUP_RESULT + tid + "/"
                + gid, GroupParticipants.class);

        assertEquals(asList(p1, p2, p3),
                r.getParticipants().stream()
                        .sorted(Comparator.comparingInt(GroupParticipantResult::getFinishPosition))
                        .map(GroupParticipantResult::getUid)
                        .map(e -> scenario.getUidPlayer().get(e))
                        .collect(toList()));
    }

    @Test
    public void justPlayOff3() {
        final TournamentScenario tournament = begin()
                .name("justPlayOff3")
                .rules(RULES_JP_S1A2G11)
                .category(c1, p1, p2, p3);
        final ImperativeSimulator simulator = isf.create(tournament);
        simulator.run(c -> c.beginTournament().checkResult(
                asList(p1, p2, p3),
                asList(p1, p3, p2)));
    }

    @Test
    public void expelP3When2Groups() { // incomplete match between 2 participants with the same points
        isf.create(begin().name("expelP3When2Groups")
                .rules(RULES_G3Q2_S1A2G11.withPlace(Optional.empty()))
                .category(c1, p1, p2, p3))
                .run(c -> c.beginTournament()
                        .scoreSet(p1, 11, p3, 3)
                        .expelPlayer(p3)
                        .checkResult(p1, p2, p3)
                        .checkTournament(Open, restState(Lost)
                                .bid(p1, Play)
                                .bid(p2, Play)
                                .bid(p3, Expl)));
    }

    @Test
    public void groupOf2() { // incomplete match between 2 participants with the same points
        isf.create(begin().name("groupOf2")
                .rules(RULES_G3Q2_S1A2G11.withPlace(Optional.empty()))
                .category(c1, p1, p2, p3))
                .run(c -> c.beginTournament()
                        .getTournamentResult());
    }
}
