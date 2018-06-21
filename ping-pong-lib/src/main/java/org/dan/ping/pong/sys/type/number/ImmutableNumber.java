package org.dan.ping.pong.sys.type.number;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode
@RequiredArgsConstructor(onConstructor = @__(@JsonCreator))
public class ImmutableNumber extends AbstractNumber {
    private final int value;

    @Override
    @JsonValue
    public int intValue() {
        return value;
    }
}
