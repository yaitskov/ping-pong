package org.dan.ping.pong.sys.scheduler;

import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import javax.inject.Inject;

public class SpringJobFactory extends SpringBeanJobFactory {
    @Inject
    private AutowireCapableBeanFactory autowireCapableBeanFactory;

    @Override
    protected Object createJobInstance(final TriggerFiredBundle bundle)
            throws Exception {
        final Object job = super.createJobInstance(bundle);
        autowireCapableBeanFactory.autowireBean(job);
        return job;
    }
}
