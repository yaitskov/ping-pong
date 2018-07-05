package org.dan.ping.pong.sys.error;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.dan.ping.pong.app.castinglots.UncheckedParticipantsError;
import org.dan.ping.pong.app.match.MatchScoredError;

import java.util.UUID;

@Getter
@Setter
@ToString
@AllArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TemplateError.class, name = "tp"),
        @JsonSubTypes.Type(value = ValidationErrors.class, name = "vl"),
        @JsonSubTypes.Type(value = UncheckedParticipantsError.class, name = "up"),
        @JsonSubTypes.Type(value = MatchScoredError.class, name = "ms")
})
public class Error {
    private String id;
    private String message;

    public Error(String msg) {
        this(UUID.randomUUID().toString(), msg);
    }

    public Error() {
        this(null);
    }
}
