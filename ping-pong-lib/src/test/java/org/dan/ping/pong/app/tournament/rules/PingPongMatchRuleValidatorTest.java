package org.dan.ping.pong.app.tournament.rules;

import static com.google.common.collect.HashMultimap.create;
import static org.dan.ping.pong.app.tournament.PingPongMatchRulesUnitTest.PING_PONG_RULE;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.Multimap;
import org.junit.Test;

public class PingPongMatchRuleValidatorTest {
    private PingPongMatchRuleValidator sut = new PingPongMatchRuleValidator();
    private final Multimap<String, ValidationError> EMPTY = create();

    @Test
    public void passPingPong() {
        final Multimap<String, ValidationError> errors = create();
        sut.validate(errors, PING_PONG_RULE);
        assertEquals(EMPTY, errors);
    }
}
