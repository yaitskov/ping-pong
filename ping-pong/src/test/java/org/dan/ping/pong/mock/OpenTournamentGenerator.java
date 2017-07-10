package org.dan.ping.pong.mock;

import org.dan.ping.pong.app.table.TestTableDao;

import java.util.List;

import javax.inject.Inject;

public class OpenTournamentGenerator {
    @Inject
    private MyLocalRest rest;

    @Inject
    private DaoEntityGenerator daoEntityGenerator;

    @Inject
    private RestEntityGenerator restEntityGenerator;

    @Inject
    private TestAdmin testAdmin;

    @Inject
    private UserSessionGenerator userSessionGenerator;

    @Inject
    private TestTableDao testTableDao;

    public GeneratedOpenTournament genOpenTour(OpenTournamentParams params) {
        final int pid = daoEntityGenerator.genPlace(testAdmin.getUid(), params.getTables());
        final int tid = daoEntityGenerator.genTournament(testAdmin.getUid(), pid, params.getProps());
        final int cid = daoEntityGenerator.genCategory(tid);
        final List<TestUserSession> participants = userSessionGenerator.generateUserSessions(
                params.getNumberOfParticipants());
        restEntityGenerator.enlistParticipants(rest, testAdmin, tid, cid, participants);
        restEntityGenerator.beginTournament(testAdmin, tid);
        return GeneratedOpenTournament.builder()
                .tid(tid)
                .cid(cid)
                .pid(pid)
                .tableIds(testTableDao.tables(pid))
                .sessions(participants)
                .build();
    }
}
