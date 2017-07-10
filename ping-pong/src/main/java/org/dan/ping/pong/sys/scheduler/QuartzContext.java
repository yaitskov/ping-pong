package org.dan.ping.pong.sys.scheduler;

import static java.lang.String.valueOf;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

import java.util.Properties;

public class QuartzContext {
    public static final String CLUSTERING_PROPERTIES = "clusteringProperties";

    @Value("${quartz.cluster.check.interval.seconds}")
    private int clusterCheckIntervalSeconds;

    @Scope(SCOPE_PROTOTYPE)
    @Bean(name = CLUSTERING_PROPERTIES)
    public Properties clusteringProperties() {
        Properties properties = new Properties();
        properties.setProperty("org.quartz.jobStore.isClustered", "true");
        properties.setProperty("org.quartz.jobStore.clusterCheckinInterval",
                valueOf(SECONDS.toMillis(clusterCheckIntervalSeconds)));
        properties.setProperty("org.quartz.scheduler.instanceId", "AUTO");

        return properties;
    }
}
