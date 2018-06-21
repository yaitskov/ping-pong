package org.dan.ping.pong.mock;

import static org.dan.ping.pong.app.tournament.TournamentRulesConst.GLOBAL;
import static org.dan.ping.pong.app.playoff.PlayOffRule.Losing1;
import static org.dan.ping.pong.app.tournament.TournamentState.Draft;
import static org.dan.ping.pong.mock.Generators.genStr;

import lombok.RequiredArgsConstructor;
import org.dan.ping.pong.app.group.GroupRules;
import org.dan.ping.pong.app.sport.SportType;
import org.dan.ping.pong.app.sport.pingpong.PingPongMatchRules;
import org.dan.ping.pong.app.place.Pid;
import org.dan.ping.pong.app.tournament.CastingLotsRulesConst;
import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.app.tournament.TournamentRules;
import org.dan.ping.pong.app.tournament.TournamentState;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class DaoEntityGeneratorWithAdmin {

    private final TestAdmin testAdmin;
    private final DaoEntityGenerator daoEntityGenerator;

    public Tid genTournament(Pid placeId) {
        return genTournament(placeId, Draft);
    }

    public Tid genTournament(Pid placeId, TournamentState state) {
        return genTournament(placeId, TournamentProps.builder()
                .state(state)
                .sport(SportType.PingPong)
                .rules(rules(2))
                .build());
    }

    public Tid genTournament(Pid placeId, TournamentState state, int quits) {
        return genTournament(placeId, TournamentProps.builder()
                .state(state)
                .sport(SportType.PingPong)
                .rules(rules(quits))
                .build());
    }

    private TournamentRules rules(int quits) {
        return TournamentRules.builder()
                .match(PingPongMatchRules.builder()
                        .setsToWin(3)
                        .minGamesToWin(11)
                        .minAdvanceInGames(2)
                        .build())
                .group(Optional.of(GroupRules.builder()
                        .groupSize(8)
                        .quits(quits)
                        .build()))
                .place(Optional.of(GLOBAL))
                .playOff(Optional.of(Losing1))
                .casting(CastingLotsRulesConst.INCREASE_SIGNUP_CASTING)
                .build();
    }

    public Tid genTournament(Pid placeId, TournamentProps props) {
        return daoEntityGenerator.genTournament(testAdmin.getUid(), placeId, props);
    }

    public Pid genPlace(int tables) {
        return daoEntityGenerator.genPlace(testAdmin.getUid(), tables);
    }

    public int genCategory(Tid tid, int cid) {
        daoEntityGenerator.genCategory(UUID.randomUUID().toString(), tid, cid);
        return cid;
    }

    public int genCity() {
        return daoEntityGenerator.genCity(genStr(), testAdmin.getUid());
    }
}
