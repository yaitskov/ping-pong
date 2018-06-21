package org.dan.ping.pong.sys.ctx.jackson;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.dan.ping.pong.sys.ctx.jackson.ObjectMapperProviderTest.Custom.BYE;
import static org.dan.ping.pong.sys.ctx.jackson.ObjectMapperProviderTest.Custom.HELLO;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class ObjectMapperProviderTest {
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class OverrideNullPolicyInField {
        @JsonInclude
        private Map<String, List<String>> m;
    }

    private ObjectMapper om;

    @Before
    public void setUp() {
        om = ObjectMapperProvider.get();
    }

    @Test
    @SneakyThrows
    public void keepMapWithEmpty() {
        assertThat(
                om.writeValueAsString(OverrideNullPolicyInField.builder()
                        .m(ImmutableMap.of("x", emptyList()))
                        .build()),
                is("{\"m\":{\"x\":[]}}"));
    }

    enum Custom {
        @JsonProperty("a")
        HELLO,
        @JsonProperty("B")
        BYE
    }

    @Test
    @SneakyThrows
    public void customEnumKeys() {
        assertThat(om.writeValueAsString(asList(HELLO, BYE)),
                is("[\"a\",\"B\"]"));
    }
}
