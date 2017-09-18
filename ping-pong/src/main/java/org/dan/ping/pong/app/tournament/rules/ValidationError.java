package org.dan.ping.pong.app.tournament.rules;

import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;
import java.util.Set;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ValidationError {
    private String template;
    private Map<String, Object> templateParams;

    public static ValidationError ofTemplate(String template) {
        return ValidationError.builder().template(template).build();
    }

    public static ValidationError ofTemplate(String template, String key, int value) {
        return ValidationError.builder().template(template)
                .templateParams(ImmutableMap.of(key, value))
                .build();
    }

    public static ValidationError ofTemplate(String template,
            String key, Set<Set<Integer>> value,
            int participants) {
        return ValidationError.builder().template(template)
                .templateParams(ImmutableMap.of(key, value, "participants", participants))
                .build();
    }
}
