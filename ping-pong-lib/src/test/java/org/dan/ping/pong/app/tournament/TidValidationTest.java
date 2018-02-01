package org.dan.ping.pong.app.tournament;

import static java.util.Collections.emptySet;
import static javax.validation.Validation.buildDefaultValidatorFactory;
import static org.dan.ping.pong.app.tournament.Tid.TOURNAMENT_ID_SHOULD_BE_A_POSITIVE_NUMBER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

import javax.validation.Validator;

public class TidValidationTest {
    Validator validator = buildDefaultValidatorFactory().getValidator();

    @Test
    public void shouldPass() {
        assertThat(validator.validate(Tid.of(1)), is(emptySet()));
    }

    @Test
    public void shouldFailOnZeroId() {
        assertThat(validator.validate(Tid.of(0)),
                hasItem(hasProperty("message", is(TOURNAMENT_ID_SHOULD_BE_A_POSITIVE_NUMBER))));
    }

    @Test
    public void shouldFailOnNegativeId() {
        assertThat(validator.validate(Tid.of(-1)),
                hasItem(hasProperty("message", is(TOURNAMENT_ID_SHOULD_BE_A_POSITIVE_NUMBER))));
    }
}
