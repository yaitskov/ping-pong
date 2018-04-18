package org.dan.ping.pong.app.match.rule.reason;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.dan.ping.pong.app.match.rule.GroupParticipantOrderServiceTest.UID3;
import static org.dan.ping.pong.app.match.rule.GroupParticipantOrderServiceTest.UID4;
import static org.dan.ping.pong.app.match.rule.GroupParticipantOrderServiceTest.UID5;
import static org.dan.ping.pong.app.match.rule.GroupParticipantOrderServiceTest.UID6;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.F2F;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.LostBalls;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.LostSets;
import static org.dan.ping.pong.app.match.rule.OrderRuleName.WonSets;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import org.dan.ping.pong.sys.ctx.jackson.ObjectMapperProvider;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

public class ReasonTest {
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class S {
        private List<Optional<Reason>> reasons;
    }

    @Test
    @SneakyThrows
    public void deserialize() {
        final ObjectMapper om = ObjectMapperProvider.get();
        final List<Reason> value = asList(new F2fReason(1, UID3, UID4),
                new InfoReason(F2F),
                new DecreasingIntScalarReason(UID6, 1, WonSets),
                new DecreasingLongScalarReason(UID4, 1232222L, LostBalls),
                new IncreasingIntScalarReason(UID5, 3, LostSets));
        assertThat(toClasses(om.readValue(om.writerFor(Reason.REASON_CHAIN_TYPE)
                        .writeValueAsString(value),
                Reason.REASON_CHAIN_TYPE)),
                is(toClasses(value)));

        S s = om.readValue(
                om.writeValueAsBytes(
                        new S(singletonList(Optional.of(new F2fReason(1, UID3, UID4))))),
                S.class);
        assertEquals( s.getReasons().get(0).get().getClass(), F2fReason.class);
    }

    private List toClasses(List objects) {
        return (List) objects.stream().map(Object::getClass).collect(toList());
    }
}
