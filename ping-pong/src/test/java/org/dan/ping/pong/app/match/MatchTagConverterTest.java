package org.dan.ping.pong.app.match;

import static org.dan.ping.pong.app.match.MatchTagConverter.MASTER_TOURNAMENT_TAG;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class MatchTagConverterTest {
    private static final MatchTag L3 = MatchTag.builder().prefix("L").number(3).build();
    private MatchTagConverter sut = new MatchTagConverter();

    @Test
    public void deserializeNullValueAsDefault() {
        assertThat(sut.from(null), is(MASTER_TOURNAMENT_TAG));
    }

    @Test
    public void deserializeValue() {
        assertThat(sut.from("L3"), is(L3));
    }

    @Test
    public void serializeDefaultToNull() {
        assertThat(sut.to(MASTER_TOURNAMENT_TAG), nullValue());
    }

    @Test
    public void serializeL3() {
        assertThat(sut.to(L3), is("L3"));
    }
}
