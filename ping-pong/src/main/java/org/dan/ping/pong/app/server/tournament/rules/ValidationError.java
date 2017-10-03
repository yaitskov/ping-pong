package org.dan.ping.pong.app.server.tournament.rules;

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
    private String message;
    private Map<String, Object> params;

    public static ValidationError ofTemplate(String template) {
        return ValidationError.builder().message(template).build();
    }

    public static ValidationError ofTemplate(String template, String key, int value) {
        return ValidationError.builder().message(template)
                .params(ImmutableMap.of(key, value))
                .build();
    }

    public static ValidationError ofTemplate(String template,
            String key, Set<Set<Integer>> value,
            int participants) {
        return ValidationError.builder().message(template)
                .params(ImmutableMap.of(key, value, "participants", participants))
                .build();
    }
}
