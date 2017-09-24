package org.dan.ping.pong.app.tournament.rules;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.HashMultimap;
import lombok.SneakyThrows;
import org.dan.ping.pong.sys.ctx.jackson.ObjectMapperProvider;
import org.dan.ping.pong.sys.error.ValidationErrors;
import org.junit.Test;

import java.util.List;

public class MultimapJacksonTest {
    private ObjectMapper om = ObjectMapperProvider.get();

    @Test
    @SneakyThrows
    public void serializeDeserializeEmpty() {
        final ValidationErrors m = om.readValue(om.writeValueAsString(
                new ValidationErrors("msg", HashMultimap.create())),
                ValidationErrors.class);
        assertEquals("msg", m.getMessage());
        assertNull(m.getField2Errors());
    }

    @Test
    @SneakyThrows
    public void serializeDeserializeFull() {
        HashMultimap<String, ValidationError> errors = HashMultimap.create();
        final List<ValidationError> values = asList(ValidationError.builder().message("a").build()
                , ValidationError.builder().message("b").build());
        errors.putAll("k", values);
        final ValidationErrors m = om.readValue(om.writeValueAsString(
                new ValidationErrors("msg", errors)),
                ValidationErrors.class);
        assertEquals("msg", m.getMessage());
        assertEquals(
                values.stream()
                        .map(ValidationError::getMessage)
                        .collect(toSet()),
                m.getField2Errors().get("k").stream()
                        .map(ValidationError::getMessage)
                        .collect(toSet()));
    }
}
