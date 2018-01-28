package org.dan.ping.pong.sys.validation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.dan.ping.pong.app.tournament.Tid;
import org.dan.ping.pong.sys.validation.TidBodyRequired.Validator;
import org.junit.Test;

public class TidBodyRequiredUnitTest {
    @Test
    public void shouldFailOnNull() {
        assertThat(new Validator().isValid(null, null), is(false));
    }

    @Test
    public void shouldPass() {
        assertThat(new Validator().isValid(Tid.of(1), null), is(true));
    }
}
