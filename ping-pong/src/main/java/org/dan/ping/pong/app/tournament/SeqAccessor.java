package org.dan.ping.pong.app.tournament;

import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;

import lombok.RequiredArgsConstructor;
import org.dan.ping.pong.sys.seqex.SequentialExecutor;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.ws.rs.container.AsyncResponse;

@RequiredArgsConstructor
public class SeqAccessor<K, V> {
    private final Cache<K, V> cache;
    private final DbUpdaterFactory dbUpdaterFactory;
    private final SequentialExecutor sequentialExecutor;

    public void update(K key, AsyncResponse response, BiConsumer<V, DbUpdater> act) {
        update(key, response, (v, batch) -> {
            act.accept(v, batch);
            return null;
        });
    }

    @Transactional(TRANSACTION_MANAGER)
    public <R> void update(K key, AsyncResponse response, BiFunction<V, DbUpdater, R> act) {
        sequentialExecutor.execute(key, () -> {
            final DbUpdater batch = dbUpdaterFactory.create()
                    .onFailure(() -> cache.invalidate(key));
            try {
                final V v = cache.load(key);
                final R retVal = act.apply(v, batch);
                batch.flush();
                response.resume(retVal);
            } catch (Exception e) {
                batch.rollback();
                response.resume(e);
            }
        });
    }

    @Transactional(readOnly = true,  transactionManager = TRANSACTION_MANAGER)
    public <R> void read(K key, AsyncResponse response, Function<V, R> act) {
        sequentialExecutor.execute(key, () -> {
            try {
                final V v = cache.load(key);
                final R retVal = act.apply(v);
                response.resume(retVal);
            } catch (Exception e) {
                response.resume(e);
            }
        });
    }
}
