package org.dan.ping.pong.app.match.rule.service.common;

import static org.dan.ping.pong.app.match.rule.GroupParticipantOrderServiceTest.UID4;
import static org.dan.ping.pong.app.match.rule.GroupParticipantOrderServiceTest.UID5;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.rule.rules.common.PickRandomlyRule;
import org.dan.ping.pong.app.match.rule.service.GroupRuleParams;
import org.junit.Test;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class PickRandomlyRuleServiceTest {
    public static final Supplier<Stream<MatchInfo>> FAILING_SUPPLIER = () -> {
        throw new IllegalStateException();
    };

    private PickRandomlyRuleService sut = new PickRandomlyRuleService();

    @Test
    public void returnIfDisambuationMatchesCreated() {
        final GroupRuleParams params = GroupRuleParams.ofParams(1, null, null, null);
        params.setDisambiguationMatchesWillBeCreated(true);
        assertThat(sut.score(FAILING_SUPPLIER, null,
                new PickRandomlyRule(), params),
                is(Optional.empty()));
    }

    @Test
    public void hashInterleaveSign() {
        assertThat(sut.hash(2, UID5), lessThan(0));
        assertThat(sut.hash(3, UID5), greaterThan(0));
        assertThat(sut.hash(4, UID5), lessThan(0));
    }

    @Test
    public void hashInterleaveOrder() {
        assertThat(sut.hash(2, UID5), lessThan(sut.hash(2, UID4)));
        assertThat(sut.hash(1, UID5), greaterThan(sut.hash(1, UID4)));
    }
}


