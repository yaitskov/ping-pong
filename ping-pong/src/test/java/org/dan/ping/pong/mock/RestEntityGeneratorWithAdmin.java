package org.dan.ping.pong.mock;

import org.dan.ping.pong.app.server.match.CompleteMatch;
import org.dan.ping.pong.app.server.match.OpenMatchForJudge;

import java.util.List;

import javax.inject.Inject;

public class RestEntityGeneratorWithAdmin {
    @Inject
    private TestAdmin testAdmin;
    @Inject
    private RestEntityGenerator restEntityGenerator;

    public void enlistParticipants(int tid, int cid, List<TestUserSession> participants) {
        restEntityGenerator.enlistParticipants(testAdmin, tid, cid, participants);
    }

    public void beginTournament(int tid) {
        restEntityGenerator.beginTournament(testAdmin, tid);
    }

    public List<OpenMatchForJudge> listOpenMatches() {
        return restEntityGenerator.listOpenMatchesForJudge(testAdmin);
    }

    public List<CompleteMatch> listCompleteMatches(int tid) {
        return restEntityGenerator.listCompleteMatches(tid);
    }

    public void generateSignInLinks(List<TestUserSession> users) {
        restEntityGenerator.generateSignInLinks(users);
    }
}
