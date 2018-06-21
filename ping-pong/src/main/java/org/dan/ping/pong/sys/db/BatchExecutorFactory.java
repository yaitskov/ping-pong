package org.dan.ping.pong.sys.db;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class BatchExecutorFactory {
    @Value("${db.batch.executor}")
    private BatchExecutorType type;

    @Bean
    public BatchExecutor batchExecutor(DSLContext jooq) {
        switch (type) {
            case BULK:
                return new TrueBatchExecutor(jooq);
            case SEQ:
                return new SeqBatchExecutor();
            default:
                throw new IllegalStateException("Type " + type + " is not supported");
        }
    }
}
