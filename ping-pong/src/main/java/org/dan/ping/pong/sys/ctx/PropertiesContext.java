
package org.dan.ping.pong.sys.ctx;

import static java.lang.System.getProperty;
import static java.nio.file.Paths.get;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.CustomEditorConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Import(CustomEditorConfigurer.class)
public class PropertiesContext {
    private static final String PING_PONG_PROPERTIES = "ping-pong.properties";

    @Bean
    public PropertySourcesPlaceholderConfigurer propertySource() {
        final PropertySourcesPlaceholderConfigurer source = new PropertySourcesPlaceholderConfigurer();
        final Path developerProperties = get(getProperty("user.home"))
                .resolve("." + PING_PONG_PROPERTIES);
        if (Files.exists(developerProperties)) {
            log.info("Load properties from [{}]", developerProperties);
            source.setLocations(new ClassPathResource(PING_PONG_PROPERTIES),
                    new FileSystemResource(developerProperties.toFile()));
        } else {
            source.setLocations(new ClassPathResource(PING_PONG_PROPERTIES));
        }
        return source;
    }
}
