package org.dan.ping.pong.sys.db;

import static com.google.common.primitives.Ints.asList;
import static java.util.stream.Collectors.toList;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Query;

import java.util.List;

@RequiredArgsConstructor
public class TrueBatchExecutor implements BatchExecutor {
    private final DSLContext jooq;

    @Override
    public List<Integer> execute(List<DbUpdateSql> updates) {
        final List<Query> queries = updates.stream()
                .map(u -> {
                    u.getLogBefore().run();
                    return u.getQuery(); })
                .collect(toList());
        return asList(jooq.batch(queries).execute());
    }
}
