package org.dan.ping.pong.app.suggestion;

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
public class CleanUpSuggestionsJob implements Job {
    @Inject
    private Clocker clocker;

    @Inject
    private SuggestionDao suggestionDao;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        final Instant cutPoint = clocker.get().minus(365, ChronoUnit.DAYS);
        log.info("Cleaned {} suggestion up entries older that {}",
                suggestionDao.cleanUp(cutPoint), cutPoint);
    }

    @Getter
    public static class CleanUpSuggestionsJobDetail extends GlobalJobDetail {
        private Class<? extends Job> jobClass = CleanUpSuggestionsJob.class;
        private String interval = "4";
        private TimeUnit intervalUnit = DAYS;
    }
}
