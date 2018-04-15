package org.dan.ping.pong.app.match.rule.service.meta;

import static org.dan.ping.pong.app.match.rule.filter.DisambiguationScope.DISAMBIGUATION_MATCHES;
import static org.dan.ping.pong.app.match.rule.filter.DisambiguationScope.ORIGIN_MATCHES;
import static org.dan.ping.pong.app.match.rule.service.common.BallsBalanceRuleServiceTest.UIDS_2_3_4;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.rule.service.GroupRuleParams;
import org.junit.Test;

import java.util.Optional;
import java.util.stream.Stream;

public class UseDisambiguationMatchesDirectiveServiceTest {
    private UseDisambiguationMatchesDirectiveService sut = new UseDisambiguationMatchesDirectiveService();

    @Test
    public void setDisambigautionFlag() {
        final GroupRuleParams params = GroupRuleParams.builder().build();
        assertThat(params.getDisambiguationMode(), is(ORIGIN_MATCHES));
        assertThat(sut.score(() -> Stream.of(new MatchInfo()),
                UIDS_2_3_4, null, params),
                is(Optional.empty()));
        assertThat(params.getDisambiguationMode(), is(DISAMBIGUATION_MATCHES));
    }

    @Test
    public void detectLackOfDisambiguationMatches() {
        final GroupRuleParams params = GroupRuleParams.builder().build();
        assertThat(params.isDisambiguationMatchesWillBeCreated(), is(false));
        assertThat(sut.score(() -> Stream.of(new MatchInfo()),
                UIDS_2_3_4, null, params),
                is(Optional.empty()));
        assertThat(params.isDisambiguationMatchesWillBeCreated(), is(true));
    }

    @Test
    public void doNotDetectLackOfDisambiguationMatches() {
        final GroupRuleParams params = GroupRuleParams.builder().build();
        assertThat(sut.score(() -> Stream.of(new MatchInfo(), new MatchInfo(), new MatchInfo()),
                UIDS_2_3_4, null, params),
                is(Optional.empty()));
        assertThat(params.isDisambiguationMatchesWillBeCreated(), is(false));
    }
}
