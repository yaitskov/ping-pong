package org.dan.ping.pong.app.sport;

import static org.dan.ping.pong.app.sport.pingpong.PingPongMatchRules.MGTW;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.dan.ping.pong.app.sport.pingpong.PingPongMatchRules;
import org.dan.ping.pong.sys.ctx.jackson.ObjectMapperProvider;
import org.junit.Test;


public class MatchRulesTest {
    private ObjectMapper om = ObjectMapperProvider.get();

    @Test
    @SneakyThrows
    public void jsonMarshall() {
        assertThat((PingPongMatchRules) om.readValue(
                om.writeValueAsString(
                        PingPongMatchRules.builder()
                                .minAdvanceInGames(1)
                                .minGamesToWin(2)
                                .minPossibleGames(3).build()),
                MatchRules.class),
                allOf(
                        isA(PingPongMatchRules.class),
                        hasProperty("minAdvanceInGames", is(1)),
                        hasProperty("minPossibleGames", is(3)),
                        hasProperty("minGamesToWin", is(2))));
    }

    @Test
    @SneakyThrows
    public void defaultPingPong() {
        assertThat((PingPongMatchRules) om.readValue("{\"" + MGTW + "\": 2}", MatchRules.class),
                allOf(
                        isA(PingPongMatchRules.class),
                        hasProperty("minGamesToWin", is(2))));
    }
}
