package org.dan.ping.pong.sys.db;

import static java.util.Optional.of;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.jooq.Query;

import java.util.Optional;
import java.util.function.Function;

@Getter
@Builder
public class DbUpdateSql implements DbUpdate {
    public static final Optional<Integer> NON_ZERO_ROWS = of(-1);
    public static final Optional<Integer> JUST_A_ROW = of(1);
    public static final Optional<Integer> JUST_2_ROWS = of(2);

    private final Runnable logBefore;
    private final Function<DbUpdateSql, Exception> onFailure;
    private final Optional<Integer> mustAffectRows;
    @NonNull
    private final Query query;

    public static class DbUpdateSqlBuilder {
        private Runnable logBefore = () -> {};
        private Function<DbUpdateSql, Exception> onFailure
                = (u) -> internalError("Expected " + u.mustAffectRows
                + " rows to be updated by query " + u.getQuery().getSQL());
        private Optional<Integer> mustAffectRows = Optional.of(1);
    }
}
