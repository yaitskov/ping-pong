package org.dan.ping.pong.app.tournament;

import lombok.RequiredArgsConstructor;
import org.dan.ping.pong.sys.db.DbUpdater;
import org.dan.ping.pong.sys.seqex.SequentialExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.ws.rs.container.AsyncResponse;

@RequiredArgsConstructor
public class SeqAccessor<K, V> {
    private final Cache<K, V> cache;
    private final DbUpdaterFactory dbUpdaterFactory;
    private final SequentialExecutor sequentialExecutor;
    private final PlatformTransactionManager transactionManager;

    public void update(K key, AsyncResponse response, BiConsumer<V, DbUpdater> act) {
        update(key, response, (v, batch) -> {
            act.accept(v, batch);
            return null;
        });
    }

    public <R> void update(K key, AsyncResponse response, BiFunction<V, DbUpdater, R> act) {
        sequentialExecutor.execute(key, () -> {
            final DbUpdater batch = dbUpdaterFactory.create()
                    .onFailure(() -> cache.invalidate(key));
            try {
                response.resume(new TransactionTemplate(transactionManager).<R>execute(
                        status -> {
                            final V v = cache.load(key);
                            final R retVal = act.apply(v, batch);
                            batch.flush();
                            return retVal;
                        }));
            } catch (Exception e) {
                batch.rollback();
                response.resume(e);
            }
        });
    }

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
