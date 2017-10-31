package org.dan.ping.pong.mock;

import org.dan.ping.pong.app.match.OpenMatchForJudgeList;

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

    public OpenMatchForJudgeList listOpenMatches(int tid) {
        return restEntityGenerator.listOpenMatchesForJudge(tid);
    }
}
