package org.dan.ping.pong.sys.ctx;

import org.dan.ping.pong.sys.scheduler.GlobalSchedulerFactoryProvider;
import org.dan.ping.pong.sys.scheduler.LocalSchedulerFactoryProvider;
import org.dan.ping.pong.sys.scheduler.QuartzContext;
import org.dan.ping.pong.sys.scheduler.SpringJobFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

@Import({QuartzContext.class,
        GlobalSchedulerFactoryProvider.class,
        LocalSchedulerFactoryProvider.class,
        SpringJobFactory.class})
public class CronContext {
    @Bean
    public SchedulerFactoryBean globalSchedulerFactoryBean(
            GlobalSchedulerFactoryProvider provider) {
        return provider.get();
    }

    @Bean
    public SchedulerFactoryBean localSchedulerFactoryBean(
            LocalSchedulerFactoryProvider provider) {
        return provider.get();
    }
}
