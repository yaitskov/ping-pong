package org.dan.ping.pong.mock;

import static org.dan.ping.pong.app.tournament.TournamentState.Draft;

import lombok.RequiredArgsConstructor;
import org.dan.ping.pong.app.tournament.TournamentState;
import org.dan.ping.pong.app.user.UserInfo;

@RequiredArgsConstructor
public class DaoEntityGeneratorWithAdmin {
    private final TestAdmin testAdmin;
    private final DaoEntityGenerator daoEntityGenerator;

    public int getAdminUid() {
        return testAdmin.getUid();
    }

    public int genTournament(int placeId) {
        return genTournament(placeId, Draft);
    }

    public int genTournament(int placeId, TournamentState state) {
        return genTournament(placeId, TournamentProps.builder().state(state).build());
    }

    public int genTournament(int placeId, TournamentProps props) {
        return daoEntityGenerator.genTournament(testAdmin.getUid(), placeId, props);
    }

    public int genPlace(int tables) {
        return daoEntityGenerator.genPlace(testAdmin.getUid(), tables);
    }

    public int genCategory(int tid) {
        return daoEntityGenerator.genCategory(tid);
    }

    public UserInfo genUser() {
        return daoEntityGenerator.genUser();
    }

    public String genUserSession(int uid) {
        return daoEntityGenerator.genUserSession(uid);
    }
}
