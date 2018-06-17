package org.dan.ping.pong.app.tournament;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

import lombok.SneakyThrows;
import org.dan.ping.pong.sys.ctx.jackson.ObjectMapperProvider;
import org.junit.Test;

public class TournamentCountersTest {
    @Test
    @SneakyThrows
    public void customPropertyName() {
        assertThat(
                ObjectMapperProvider.get()
                        .writeValueAsString(new TournamentCounters()),
                containsString("\"c\":0"));

    }
}
