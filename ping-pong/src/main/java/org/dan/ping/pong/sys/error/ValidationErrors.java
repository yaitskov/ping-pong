package org.dan.ping.pong.sys.error;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import lombok.Getter;
import lombok.Setter;
import org.dan.ping.pong.app.tournament.rules.MultimapJsonSerializer;
import org.dan.ping.pong.app.tournament.rules.ValidationError;
import org.dan.ping.pong.app.tournament.rules.ValidationErrorsDeserializer;

@Getter
@Setter
public class ValidationErrors extends Error {
    @JsonSerialize(using = MultimapJsonSerializer.class)
    @JsonDeserialize(using = ValidationErrorsDeserializer.class)
    private Multimap<String, ValidationError> field2Errors;

    public ValidationErrors() {}

    public ValidationErrors(String message,
            Multimap<String, ValidationError> errors) {
        super(message);
        this.field2Errors = errors;
    }
}
