package org.dan.ping.pong.app.tournament;

import javax.inject.Inject;

public class OpenTournamentService {
    @Inject
    private TournamentDao tournamentDao;

    public OpenTournamentMemState load(int tid) {
        return null;
    }

    public void invalidate(int tid) {

    }
}
