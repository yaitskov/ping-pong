package org.dan.ping.pong.app.server.tournament.rules;

public class ValidationErrorsDeserializer extends MultimapJsonDeserializer<ValidationError> {
    public ValidationErrorsDeserializer() {
        super(ValidationError.class);
    }
}
