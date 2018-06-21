package org.dan.ping.pong.app.match.dispute;

import static org.dan.ping.pong.jooq.Tables.MATCH_DISPUTE;

import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.sys.db.DbUpdater;
import org.jooq.DSLContext;

import javax.inject.Inject;

public class MatchDisputeDaoServer implements MatchDisputeDao {
    @Inject
    private DSLContext jooq;

    @Override
    public void create(Tid tid, DisputeMemState dispute, DbUpdater batch) {
        batch.equals(jooq
                .insertInto(MATCH_DISPUTE, MATCH_DISPUTE.DID,
                        MATCH_DISPUTE.CREATED, MATCH_DISPUTE.MID,
                        MATCH_DISPUTE.PLAINTIFF, MATCH_DISPUTE.PLAINTIFF_COMMENT,
                        MATCH_DISPUTE.PROPOSED_SCORE, MATCH_DISPUTE.STATUS,
                        MATCH_DISPUTE.TID)
                .values(dispute.getDid(), dispute.getCreated(), dispute.getMid(),
                        dispute.getPlaintiff(),
                        dispute.getPlaintiffComment(), dispute.getProposedScore(),
                        dispute.getStatus(), tid));
    }
}
