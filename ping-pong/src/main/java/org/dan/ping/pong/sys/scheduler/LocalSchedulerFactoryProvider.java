package org.dan.ping.pong.sys.scheduler;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

@Named
public class LocalSchedulerFactoryProvider extends SchedulerFactoryBeanProvider {
    @Inject
    private Optional<Set<LocalJobDetail>> scheduledJobs;

    @Override
    protected Set<? extends QuartzJobDetail> getScheduleJobs() {
        return scheduledJobs.orElse(Collections.emptySet());
    }

    @Override
    protected void setProperties(SchedulerFactoryBean bean) {
        // nop
    }

    @Bean(name = "localSchedulerFactoryBean")
    @Override
    public SchedulerFactoryBean get() {
        return super.get();
    }
}
