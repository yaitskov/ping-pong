package org.dan.ping.pong.mock;

import static org.dan.ping.pong.app.tournament.TournamentState.Draft;
import static org.dan.ping.pong.mock.Generators.genStr;

import lombok.RequiredArgsConstructor;
import org.dan.ping.pong.app.tournament.TournamentState;

import java.util.UUID;

@RequiredArgsConstructor
public class DaoEntityGeneratorWithAdmin {
    private final TestAdmin testAdmin;
    private final DaoEntityGenerator daoEntityGenerator;

    public int genTournament(int placeId) {
        return genTournament(placeId, Draft);
    }

    public int genTournament(int placeId, TournamentState state) {
        return genTournament(placeId, TournamentProps.builder().state(state).build());
    }

    public int genTournament(int placeId, TournamentState state, int quits) {
        return genTournament(placeId, TournamentProps.builder()
                .state(state).quitsFromGroup(quits)
                .build());
    }

    public int genTournament(int placeId, TournamentProps props) {
        return daoEntityGenerator.genTournament(testAdmin.getUid(), placeId, props);
    }

    public int genPlace(int tables) {
        return daoEntityGenerator.genPlace(testAdmin.getUid(), tables);
    }

    public int genCategory(int tid) {
        return daoEntityGenerator.genCategory(UUID.randomUUID().toString(), tid);
    }

    public int genCity() {
        return daoEntityGenerator.genCity(genStr(), testAdmin.getUid());
    }
}
