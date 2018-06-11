package org.dan.ping.pong.sys.warmup;

import static java.util.concurrent.TimeUnit.DAYS;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.dan.ping.pong.sys.scheduler.GlobalJobDetail;
import org.dan.ping.pong.util.time.Clocker;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

@Slf4j
@DisallowConcurrentExecution
public class CleanUpWarmUpJob implements Job {
    @Inject
    private Clocker clocker;

    @Inject
    private WarmUpDao warmUpDao;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        final Instant cutPoint = clocker.get().minus(30, ChronoUnit.DAYS);
        log.info("Cleaned {} warm-up entries older that {}",
                warmUpDao.cleanOlderThan(cutPoint), cutPoint);
    }

    @Getter
    public static class CleanUpWarmUpJobDetail extends GlobalJobDetail {
        private Class<? extends Job> jobClass = CleanUpWarmUpJob.class;
        private String interval = "4";
        private TimeUnit intervalUnit = DAYS;
    }
}
