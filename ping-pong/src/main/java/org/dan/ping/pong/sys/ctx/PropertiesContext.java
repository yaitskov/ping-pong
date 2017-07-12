
package org.dan.ping.pong.sys.ctx;

import static java.util.Collections.sort;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.CustomEditorConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Import(CustomEditorConfigurer.class)
public class PropertiesContext {
    private static final String PING_PONG_PROPERTIES = "ping-pong.properties";
    private static final int DEFAULT_PROPERTY_PRIORITY = 1;

    @Bean
    public PropertyFileRef defaultProperties() {
        return PropertyFileRef.builder()
                .priority(DEFAULT_PROPERTY_PRIORITY)
                .resource(new ClassPathResource(PING_PONG_PROPERTIES))
                .build();
    }

    @Bean
    public PropertySourcesPlaceholderConfigurer propertySource(List<PropertyFileRef> refs) {
        final PropertySourcesPlaceholderConfigurer source = new PropertySourcesPlaceholderConfigurer();
        final Set<Integer> usedPriorities = new HashSet<>();
        sort(refs);
        log.info("Load properties from [{}]", refs);
        source.setLocations(refs.stream()
                .map(p -> {
                    if (!usedPriorities.add(p.getPriority())) {
                        throw new IllegalStateException("Priority " + p.getPriority()
                                + " is used multiple times");
                    }
                    return p;
                })
                .map(PropertyFileRef::getResource)
                .toArray(Resource[]::new));
        return source;
    }
}
