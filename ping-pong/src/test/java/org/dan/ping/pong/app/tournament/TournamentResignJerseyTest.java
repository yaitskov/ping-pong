package org.dan.ping.pong.app.tournament;

import static org.dan.ping.pong.app.bid.BidState.Quit;
import static org.dan.ping.pong.app.match.MatchState.Over;
import static org.dan.ping.pong.mock.simulator.HookDecision.Score;
import static org.dan.ping.pong.mock.simulator.HookDecision.Skip;
import static org.dan.ping.pong.mock.simulator.Player.p1;
import static org.dan.ping.pong.mock.simulator.Player.p2;
import static org.dan.ping.pong.mock.simulator.Player.p3;
import static org.dan.ping.pong.mock.simulator.Player.p4;
import static org.dan.ping.pong.mock.simulator.PlayerCategory.c1;
import static org.dan.ping.pong.mock.simulator.SimulatorParams.T_1_Q_1_G_2;
import static org.dan.ping.pong.mock.simulator.SimulatorParams.T_1_Q_1_G_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

import lombok.RequiredArgsConstructor;
import org.dan.ping.pong.JerseySpringTest;
import org.dan.ping.pong.app.bid.BidDao;
import org.dan.ping.pong.app.match.ForTestMatchDao;
import org.dan.ping.pong.app.match.MatchDao;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.mock.simulator.Hook;
import org.dan.ping.pong.mock.simulator.HookCallback;
import org.dan.ping.pong.mock.simulator.HookDecision;
import org.dan.ping.pong.mock.simulator.MatchMetaInfo;
import org.dan.ping.pong.mock.simulator.PlayHook;
import org.dan.ping.pong.mock.simulator.Player;
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
public class TournamentResignJerseyTest extends AbstractSpringJerseyTest {
    @Inject
    private Simulator simulator;

    @Inject
    private TournamentService tournamentService;

    @Inject
    private BidDao bidDao;

    @Test
    public void resignInDraft() {
        final TournamentScenario scenario = TournamentScenario.begin()
                .doNotBegin()
                .name("resignInDraft")
                .category(c1, p1, p2, p3);

        simulator.simulate(T_1_Q_1_G_8, scenario);
        final int uid = scenario.getPlayersSessions().get(p1).getUid();
        tournamentService.leaveTournament(uid, scenario.getTid(), Quit);

        assertEquals(Optional.of(Quit), bidDao.getState(scenario.getTid(), uid));
    }

    @Inject
    private MatchDao matchDao;

    @Inject
    private ForTestMatchDao forTestMatchDao;

    @Test
    public void resignInGroupMiddle() {
        final TournamentScenario scenario = TournamentScenario.begin()
                .name("resignInGroupMiddle")
                .category(c1, p1, p2, p3)
                .w31(p1, p2)
                .w30(p2, p3)
                .quitsGroup(p2)
                .pause(p1, p2, PlayHook.builder()
                        .type(Hook.BeforeScore)
                        .callback((s, metaInfo) -> checkQuit(s, metaInfo, p1, p3))
                        .build())
                .champions(c1, p2);

        simulator.simulate(T_1_Q_1_G_8, scenario);
    }

    @Test
    public void resignInLastGroupGame() {
        final TournamentScenario scenario = TournamentScenario.begin()
                .name("resignInLastGroupGame")
                .category(c1, p1, p2, p3)
                .w30(p1, p2)
                .w32(p2, p3)
                .w30(p1, p3)
                .quitsGroup(p2)
                .pause(p1, p3, PlayHook.builder()
                        .type(Hook.BeforeScore)
                        .callback((s, metaInfo) -> checkQuit(s, metaInfo, p1, p3))
                        .build())
                .champions(c1, p2);

        simulator.simulate(T_1_Q_1_G_8, scenario);
    }

    private HookDecision checkQuit(TournamentScenario s, MatchMetaInfo metaInfo, Player p1, Player p3) {
        final int uid1 = s.getPlayersSessions().get(p1).getUid();
        tournamentService.leaveTournament(uid1, s.getTid(), Quit);
        assertEquals(Optional.of(Quit), bidDao.getState(s.getTid(), uid1));
        assertEquals(Optional.of(Over), matchDao.getById(metaInfo.getOpenMatch().getMid())
                .map(MatchInfo::getState));

        final int uid3 = s.getPlayersSessions().get(p3).getUid();
        assertThat(forTestMatchDao.findByParticipants(s.getTid(), uid1, uid3),
                allOf(
                        hasProperty("state", is(Over)),
                        hasProperty("scores",
                                allOf(
                                        hasItem(allOf(
                                                hasProperty("uid", is(uid3)),
                                                hasProperty("won", is(1)),
                                                hasProperty("score", is(0)))),
                                        hasItem(allOf(
                                                hasProperty("uid", is(uid1)),
                                                hasProperty("won", is(-1)),
                                                hasProperty("score", is(0))))))));
        return Skip;
    }

    @RequiredArgsConstructor
    private class ResignAfter2 implements HookCallback {
        private final int resignAfter;
        private final Player player;
        private int counter;

        @Override
        public HookDecision apply(TournamentScenario s, MatchMetaInfo metaInfo) {
            if (++counter == resignAfter) {
                final int uid1 = s.getPlayersSessions().get(player).getUid();
                tournamentService.leaveTournament(uid1, s.getTid(), Quit);
            }
            return Score;
        }
    }

    @Test
    public void resignAfterAllOwnGroupGames() {
        final ResignAfter2 resignAfter2  = new ResignAfter2(2, p1);
        final TournamentScenario scenario = TournamentScenario.begin()
                .name("quitAfterAllOwnGroup")
                .category(c1, p1, p2, p3)
                .w30(p1, p2)
                .w32(p2, p3)
                .w30(p1, p3)
                .quitsGroup(p2)
                .pause(p1, p2, PlayHook.builder()
                        .type(Hook.AfterScore)
                        .callback(resignAfter2)
                        .build())
                .pause(p1, p3, PlayHook.builder()
                        .type(Hook.AfterScore)
                        .callback(resignAfter2)
                        .build())
                .champions(c1, p2);

        simulator.simulate(T_1_Q_1_G_8, scenario);
    }

    @Test
    public void resignAfterAllOwnGroupGamesWinsCountFirst() {
        final ResignAfter2 resignAfter2  = new ResignAfter2(2, p1);
        final TournamentScenario scenario = TournamentScenario.begin()
                .name("quitAfterAllOwnGroupL")
                .category(c1, p1, p2, p3)
                .w30(p1, p2)
                .w32(p2, p3)
                .w32(p1, p3)
                .quitsGroup(p2)
                .pause(p1, p2, PlayHook.builder()
                        .type(Hook.AfterScore)
                        .callback(resignAfter2)
                        .build())
                .pause(p1, p3, PlayHook.builder()
                        .type(Hook.AfterScore)
                        .callback(resignAfter2)
                        .build())
                .champions(c1, p2);

        simulator.simulate(T_1_Q_1_G_8, scenario);
    }

    @Test
    public void resignFromActiveMatchForGold() {
        final TournamentScenario scenario = TournamentScenario.begin()
                .name("resignFromActiveGold")
                .category(c1, p1, p2, p3, p4)
                .w30(p1, p2)
                .w32(p3, p4)
                .quitsGroup(p1, p3)
                .w32(p1, p3)
                .champions(c1, p3, p1)
                .pause(p1, p3, PlayHook.builder()
                        .type(Hook.BeforeScore)
                        .callback((s, meta) -> {
                            final int uid1 = s.getPlayersSessions().get(p1).getUid();
                            tournamentService.leaveTournament(uid1, s.getTid(), Quit);
                            return Skip;
                        })
                        .build());

        simulator.simulate(T_1_Q_1_G_2, scenario);
    }
}
