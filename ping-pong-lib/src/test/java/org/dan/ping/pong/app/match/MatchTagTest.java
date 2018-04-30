package org.dan.ping.pong.app.match;

import static org.dan.ping.pong.app.group.GroupService.OM_TAG;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.dan.ping.pong.sys.ctx.jackson.ObjectMapperProvider;
import org.junit.Test;

import java.util.Map;

public class MatchTagTest {
    private ObjectMapper om = ObjectMapperProvider.get();

    @NoArgsConstructor
    @AllArgsConstructor
    public static class WithOptionalMapKey {
        @Getter
        public Map<MatchTag, String> m;
    }

    @Test
    @SneakyThrows
    public void serializedEmptyToNull() {
        assertThat(
                om.readValue(
                        om.writeValueAsString(
                                new WithOptionalMapKey(
                                        ImmutableMap.of(OM_TAG, "HELL"))),
                        WithOptionalMapKey.class),
                hasProperty("m",
                        hasEntry(is(OM_TAG), is("HELL"))));
    }
}
