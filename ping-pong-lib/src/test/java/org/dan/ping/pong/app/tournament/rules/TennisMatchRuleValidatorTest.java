package org.dan.ping.pong.app.tournament.rules;

import static com.google.common.collect.HashMultimap.create;
import static org.dan.ping.pong.app.sport.tennis.TennisSportTest.CLASSIC_TENNIS_RULES;
import static org.dan.ping.pong.app.tournament.rules.GroupRuleValidator.OUT_OF_RANGE;
import static org.dan.ping.pong.app.tournament.rules.TennisMatchRuleValidator.SUPER_TIEBREAK_GAMES;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.google.common.collect.Multimap;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsMapContaining;
import org.junit.Test;

import java.util.Optional;

public class TennisMatchRuleValidatorTest {
    private TennisMatchRuleValidator sut = new TennisMatchRuleValidator();
    private final Multimap<String, ValidationError> EMPTY = create();

    @Test
    public void pass() {
        final Multimap<String, ValidationError> errors = create();
        sut.validate(errors, CLASSIC_TENNIS_RULES);
        assertEquals(EMPTY, errors);
    }

    @Test
    public void failsOnSuperTieBreak() {
        final Multimap<String, ValidationError> errors = create();
        sut.validate(errors, CLASSIC_TENNIS_RULES
                .withSuperTieBreakGames(Optional.of(0)));
        assertThat(errors.asMap(), IsMapContaining.hasEntry(Matchers.is(SUPER_TIEBREAK_GAMES),
                hasItem(hasProperty("message", Matchers.is(OUT_OF_RANGE)))));
    }
}
