package org.dan.ping.pong.util.json;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import org.dan.ping.pong.sys.ctx.jackson.ObjectMapperProvider;
import org.junit.Test;

public class DoubleContextSerializerTest {
    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class X {
        @Precision(2)
        @JsonSerialize(using = DoubleContextSerializer.class)
        private double f;
    }

    @Test
    @SneakyThrows
    public void precisionObeyed() {
        final ObjectMapper om = ObjectMapperProvider.get();
        assertThat(
                om.writeValueAsString(new X(0.1234567)),
                is("{\"f\":0.12}"));
    }
}
