package org.dan.ping.pong.sys.scheduler;

import static org.dan.ping.pong.sys.db.DbContext.DATA_SOURCE;
import static org.dan.ping.pong.sys.db.DbContext.TRANSACTION_MANAGER;
import static org.dan.ping.pong.sys.scheduler.QuartzContext.CLUSTERING_PROPERTIES;

import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.util.Collections;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;

public class GlobalSchedulerFactoryProvider extends SchedulerFactoryBeanProvider {
    @Inject
    private Optional<Set<GlobalJobDetail>> scheduledJobs;

    @Inject
    @Named(DATA_SOURCE)
    private DataSource dataSource;

    @Inject
    @Named(TRANSACTION_MANAGER)
    private DataSourceTransactionManager txManager;

    @Inject
    @Named(CLUSTERING_PROPERTIES)
    private Properties clusteringProperties;

    @Override
    protected Set<? extends QuartzJobDetail> getScheduleJobs() {
        return scheduledJobs.orElse(Collections.emptySet());
    }

    @Override
    protected void setProperties(SchedulerFactoryBean bean) {
        bean.setQuartzProperties(clusteringProperties);
        bean.setDataSource(dataSource);
        bean.setTransactionManager(txManager);
    }
}
