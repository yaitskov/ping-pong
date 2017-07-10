package org.dan.ping.pong.sys.scheduler;

import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

public class IntervalTriggerFactory
        extends SimpleTriggerFactoryBean
        implements TriggerFactory {
    public IntervalTriggerFactory(long msInterval) {
        setRepeatInterval(msInterval);
    }
}
