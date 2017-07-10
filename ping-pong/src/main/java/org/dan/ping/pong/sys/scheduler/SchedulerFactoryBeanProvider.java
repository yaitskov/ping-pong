package org.dan.ping.pong.sys.scheduler;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Throwables.propagate;
import static java.lang.Long.parseLong;
import static java.util.stream.Collectors.toList;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Trigger;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.text.ParseException;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

@Slf4j
public abstract class SchedulerFactoryBeanProvider implements Provider<SchedulerFactoryBean> {
    @Inject
    private SpringJobFactory jobFactory;

    protected abstract Set<? extends QuartzJobDetail> getScheduleJobs();

    protected abstract void setProperties(SchedulerFactoryBean bean);

    @Override
    public SchedulerFactoryBean get() {
        SchedulerFactoryBean result = new SchedulerFactoryBean();
        setProperties(result);
        log.info("Scheduler [{}] starts jobs {}",
                getClass().getSimpleName(),
                getScheduleJobs().stream()
                        .map(Object::getClass)
                        .map(Class::getSimpleName)
                        .collect(toList()));
        result.setTriggers(getScheduleJobs().stream()
                .map(job -> getTrigger(job))
                .toArray(Trigger[]::new));
        result.setJobFactory(jobFactory);
        return result;
    }

    private static Trigger getTrigger(QuartzJobDetail jobDetail) {

        Class<? extends Job> jobClass = jobDetail.getJobClass();
        String name = jobDetail.getName();

        JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
        jobDetailFactoryBean.setJobClass(jobClass);
        jobDetailFactoryBean.setName(name);
        jobDetailFactoryBean.setGroup(jobDetail.getGroup());
        jobDetailFactoryBean.afterPropertiesSet();
        jobDetailFactoryBean.setJobDataMap(jobDetail.getJobDataMap());

        TriggerFactory triggerFactory = createTriggerFactory(jobDetail);
        triggerFactory.setName(name + "Trigger");
        triggerFactory.setGroup(jobDetail.getGroup());
        triggerFactory.setJobDetail(jobDetailFactoryBean.getObject());

        try {
            triggerFactory.afterPropertiesSet();
        } catch (ParseException e) {
            log.error("Failed to parse cron expression {} on class {} because of: {}",
                    jobDetail.getCron(),
                    jobDetail,
                    e.getMessage());
            propagate(e);
        }
        return triggerFactory.getObject();
    }

    private static TriggerFactory createTriggerFactory(QuartzJobDetail jobDetail) {
        final String rawCron = jobDetail.getCron();
        final String periodic = jobDetail.getInterval();
        checkArgument(!rawCron.isEmpty() ^ !periodic.isEmpty(),
                "Use cron xor interval");
        if (rawCron.isEmpty()) {
            return new IntervalTriggerFactory(
                    jobDetail.getIntervalUnit().toMillis(
                            parseLong(periodic)));
        }
        return new CronTriggerFactory(rawCron);
    }

    @Named
    @Getter
    private static class LocalDummyJobDetail extends LocalJobDetail {
        private String cron = "0 0 0 1 1 ?";
        private Class<? extends Job> jobClass = LocalDummyJob.class;
    }

    private static class LocalDummyJob extends QuartzJobBean {
        @Override
        protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        }
    }

    @Named
    @Getter
    private static class GlobalDummyJobDetail extends GlobalJobDetail {
        private String cron = "0 0 0 1 1 ?";
        private Class<? extends Job> jobClass = GlobalDummyJob.class;
    }

    private static class GlobalDummyJob extends QuartzJobBean {
        @Override
        protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        }
    }
}
