package org.dan.ping.pong.app.tournament;

import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.dan.ping.pong.sys.ctx.jackson.ObjectMapperProvider;
import org.junit.Test;

public class TidJacksonTest {
    @Test
    @SneakyThrows
    public void scalar() {
        ObjectMapper om = ObjectMapperProvider.get();
        final String actual = om.writeValueAsString(new Tid(123));
        assertThat(actual, is("123"));
        assertThat(om.readValue(actual, Tid.class),
                hasProperty("tid", is(123)));
    }
}
