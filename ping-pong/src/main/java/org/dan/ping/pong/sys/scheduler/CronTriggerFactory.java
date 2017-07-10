package org.dan.ping.pong.sys.scheduler;

import org.springframework.scheduling.quartz.CronTriggerFactoryBean;

public class CronTriggerFactory
        extends CronTriggerFactoryBean
        implements TriggerFactory {
    public CronTriggerFactory(String schedule) {
        setCronExpression(schedule);
    }
}
