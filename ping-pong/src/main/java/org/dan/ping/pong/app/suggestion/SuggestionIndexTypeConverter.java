package org.dan.ping.pong.app.suggestion;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;
import static org.dan.ping.pong.app.suggestion.SuggestionIndexType.F3L3;
import static org.dan.ping.pong.app.suggestion.SuggestionIndexType.Initials;
import static org.dan.ping.pong.sys.error.PiPoEx.internalError;

import com.google.common.collect.ImmutableMap;
import org.jooq.impl.AbstractConverter;

import java.util.HashMap;
import java.util.Map;

public class SuggestionIndexTypeConverter extends AbstractConverter<String, SuggestionIndexType> {
    private static final Map<String, SuggestionIndexType> STR_TO_ENUM = new HashMap<>(
            ImmutableMap.<String, SuggestionIndexType>builder()
                    .put("in", Initials)
                    .put("f3", F3L3)
                    .build());

    private static final Map<SuggestionIndexType, String> ENUM_TO_STR = STR_TO_ENUM
            .entrySet().stream()
            .collect(toMap(Map.Entry::getValue, Map.Entry::getKey));

    public SuggestionIndexTypeConverter() {
        super(String.class, SuggestionIndexType.class);
    }

    @Override
    public SuggestionIndexType from(String s) {
        return ofNullable(STR_TO_ENUM.get(s))
                .orElseThrow(() -> internalError(
                        "suggestion index [" + s + "] not valid"));
    }

    @Override
    public String to(SuggestionIndexType type) {
        return ofNullable(ENUM_TO_STR.get(type))
                .orElseThrow(() -> internalError(
                        "suggestion index [" + type + "] not persistable"));
    }
}
