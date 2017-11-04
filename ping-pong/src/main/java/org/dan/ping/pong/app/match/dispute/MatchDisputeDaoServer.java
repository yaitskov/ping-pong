package org.dan.ping.pong.app.match.dispute;

import static ord.dan.ping.pong.jooq.Tables.MATCH_DISPUTE;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;

import org.dan.ping.pong.app.tournament.Tid;
import org.jooq.DSLContext;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

public class MatchDisputeDaoServer implements MatchDisputeDao {
    @Inject
    private DSLContext jooq;

    @Override
    @Transactional(TRANSACTION_MANAGER)
    public DisputeId create(Tid tid, DisputeMemState dispute) {
        return jooq
                .insertInto(MATCH_DISPUTE,
                        MATCH_DISPUTE.CREATED, MATCH_DISPUTE.MID,
                        MATCH_DISPUTE.PLAINTIFF, MATCH_DISPUTE.PLAINTIFF_COMMENT,
                        MATCH_DISPUTE.PROPOSED_SCORE, MATCH_DISPUTE.STATUS,
                        MATCH_DISPUTE.TID)
                .values(dispute.getCreated(), dispute.getMid(), dispute.getPlaintiff(),
                        dispute.getPlaintiffComment(), dispute.getProposedScore(),
                        dispute.getStatus(), tid)
                .returning(MATCH_DISPUTE.DID)
                .fetchOne()
                .getDid();
    }
}
