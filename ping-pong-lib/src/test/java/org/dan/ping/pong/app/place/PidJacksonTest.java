package org.dan.ping.pong.app.place;

import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.dan.ping.pong.sys.ctx.jackson.ObjectMapperProvider;
import org.junit.Test;

public class PidJacksonTest {
    @Test
    @SneakyThrows
    public void scalar() {
        ObjectMapper om = ObjectMapperProvider.get();
        final String actual = om.writeValueAsString(new Pid(123));
        assertThat(actual, is("123"));
        assertThat(om.readValue(actual, Pid.class),
                hasProperty("pid", is(123)));
    }
}
