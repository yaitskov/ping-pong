package org.dan.ping.pong.sys.ctx;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.config.CustomEditorConfigurer;
import org.springframework.context.annotation.Bean;

import java.time.Duration;

public class TypeAdapterCtx {
    @Bean
    public CustomEditorConfigurer customEditorConfigurer() {
        CustomEditorConfigurer configurer = new CustomEditorConfigurer();
        configurer.setCustomEditors(ImmutableMap.of(
                Duration.class, DurationEditorSupport.class));
        return configurer;
    }
}
