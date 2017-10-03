package org.dan.ping.pong.app.server.tournament;

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
public class DbUpdate {
    public static final Optional<Integer> NON_ZERO_ROWS = of(-1);
    public static final Optional<Integer> JUST_A_ROW = of(1);
    public static final Optional<Integer> JUST_2_ROWS = of(2);

    private final Runnable logBefore;
    private final Function<DbUpdate, Exception> onFailure;
    private final Optional<Integer> mustAffectRows;
    @NonNull
    private final Query query;

    public static class DbUpdateBuilder {
        private Runnable logBefore = () -> {};
        private Function<DbUpdate, Exception> onFailure
                = (u) -> internalError("Expected " + u.mustAffectRows
                + " rows to be updated by query " + u.getQuery().getSQL());
        private Optional<Integer> mustAffectRows = Optional.of(1);
    }
}
