package org.dan.ping.pong.app.suggestion;

import static org.dan.ping.pong.jooq.Tables.USERS;
import static org.dan.ping.pong.jooq.tables.SuggestName.SUGGEST_NAME;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;
import static org.dan.ping.pong.sys.db.DbUpdateSql.NON_ZERO_ROWS;

import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.user.UserLink;
import org.dan.ping.pong.sys.db.DbUpdateSql;
import org.dan.ping.pong.sys.db.DbUpdater;
import org.jooq.DSLContext;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import javax.inject.Inject;

@Slf4j
public class SuggestionDao {
    @Inject
    private DSLContext jooq;

    @Transactional(TRANSACTION_MANAGER)
    public void createIndexRow(SuggestionIndexType type, String pattern,
            Uid requesterUid, Uid targetUid, Instant now, DbUpdater batch) {
        batch.exec(DbUpdateSql.builder()
                .logBefore(() -> log.info("suggestion index [{}][{}]", pattern, type))
                .mustAffectRows(NON_ZERO_ROWS)
                .query(jooq.insertInto(
                        SUGGEST_NAME,
                        SUGGEST_NAME.REQUESTER_UID, SUGGEST_NAME.UID,
                        SUGGEST_NAME.TYPE,
                        SUGGEST_NAME.PATTERN).
                        values(requesterUid, targetUid, type, pattern)
                        .onDuplicateKeyUpdate()
                        .set(SUGGEST_NAME.CREATED, now))
                .build());
    }

    @Transactional(TRANSACTION_MANAGER)
    public int cleanUp(Instant olderThan) {
        return jooq.deleteFrom(SUGGEST_NAME)
                .where(SUGGEST_NAME.CREATED.le(olderThan))
                .execute();
    }

    @Transactional(readOnly = true, transactionManager = TRANSACTION_MANAGER)
    public List<UserLink> findUsers(
            Uid requester,
            PatternInterpretation pattern,
            List<Uid> skipUids,
            PageAdr page) {
        return jooq.select(USERS.NAME, USERS.UID)
                .from(SUGGEST_NAME).innerJoin(USERS)
                .on(SUGGEST_NAME.UID.eq(USERS.UID))
                .where(SUGGEST_NAME.REQUESTER_UID.eq(requester),
                        SUGGEST_NAME.TYPE.eq(pattern.getIdxType()),
                        pattern.isLike()
                                ? SUGGEST_NAME.PATTERN.likeIgnoreCase(
                                        pattern.getPattern())
                                : SUGGEST_NAME.PATTERN.eq(
                                        pattern.getPattern()),
                        SUGGEST_NAME.UID.notIn(skipUids))
                .offset(page.offset())
                .limit(page.getSize())
                .fetch()
                .map(r -> UserLink.builder()
                        .name(r.get(USERS.NAME))
                        .uid(r.get(USERS.UID))
                        .build());
    }
}
