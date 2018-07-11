package org.dan.ping.pong.app.tournament;

import static java.util.Arrays.asList;
import static org.dan.ping.pong.app.match.IdentifiedScore.scoreOf;
import static org.dan.ping.pong.app.sport.pingpong.PingPongMatchRules.BALLS_CANNOT_BE_LESS_THAN;
import static org.dan.ping.pong.app.sport.pingpong.PingPongMatchRules.DIFFERENCE_BETWEEN_BALLS_CANNOT_BE_LESS_THAN;
import static org.dan.ping.pong.app.sport.pingpong.PingPongMatchRules.WINNER_SHOULD_HAVE_AT_LEAST_N_BALLS;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.match.Mid;
import org.dan.ping.pong.app.match.SetScoreReq;
import org.dan.ping.pong.app.sport.pingpong.PingPongMatchRules;
import org.dan.ping.pong.app.sport.pingpong.PingPongSport;
import org.dan.ping.pong.app.sport.tennis.TennisSport;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;
import java.util.Optional;

public class PingPongSportUnitTest {
    public static final PingPongMatchRules PING_PONG_RULE = PingPongMatchRules.builder()
            .minGamesToWin(11)
            .minPossibleGames(0)
            .minAdvanceInGames(2)
            .setsToWin(3)
            .build();

    private static final PingPongSport RULES = new PingPongSport();

    private static final Bid BID_A = new Bid(1);
    private static final Bid BID_B = new Bid(2);
    public static final PingPongSport PING_PONG_SPORT = new PingPongSport();

    @Test
    public void validatePass() {
        validate(11, 0);
        validate(0, 11);
        validate(9, 11);
        validate(11, 9);
        validate(15, 13);
    }

    @Test
    public void validateFailToManySets() {
        thrown.expect(hasProperty("message",
                containsString(TennisSport.TO_MANY_SETS)));
        RULES.validate(PING_PONG_RULE,
                match(asList(11, 11, 11, 11),
                        asList(0, 1, 2, 3)));
    }

    private void validate(int a, int b) {
        RULES.validate(PING_PONG_RULE, match(a, b));
    }

    private MatchInfo match(int a, int b) {
        return match(asList(a), asList(b));
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void validateFailsOnMaxGamesToSmall() {
        thrown.expect(hasProperty("message",
                containsString(WINNER_SHOULD_HAVE_AT_LEAST_N_BALLS)));
        validate(10, 0);
    }

    @Test
    public void validateFailsOnNegaiteveGames() {
        thrown.expect(hasProperty("message",
                containsString(BALLS_CANNOT_BE_LESS_THAN)));
        validate(10, -1);
    }

    @Test
    public void validateFailsOnCloseGames() {
        thrown.expect(hasProperty("message",
                containsString(DIFFERENCE_BETWEEN_BALLS_CANNOT_BE_LESS_THAN)));
        validate(11, 10);
    }

    @Test
    public void cannotFindWinner1() {
        cannotFindWinner(asList(11), asList(0));
    }

    @Test
    public void cannotFindWinner2() {
        cannotFindWinner(asList(11, 9, 11), asList(0, 11, 0));
    }

    @Test
    public void cannotFindWinner3() {
        cannotFindWinner(asList(11, 9, 11, 0), asList(0, 11, 0, 11));
    }

    @Test
    public void uidAWinsConfidently() {
        findWinner(BID_A, asList(11, 11, 11), asList(0, 1, 2));
    }

    @Test
    public void uidBBarelyWins() {
        findWinner(BID_B, asList(11, 11, 9, 14, 12), asList(0, 0, 11, 16, 14));
    }

    private void cannotFindWinner(List<Integer> a, List<Integer> b) {
        assertEquals(Optional.empty(),
                PING_PONG_SPORT.findWinnerId(PING_PONG_RULE,
                        PING_PONG_SPORT.calcWonSets(match(a, b).getParticipantIdScore())));
    }

    private void findWinner(Bid bid, List<Integer> a, List<Integer> b) {
        assertEquals(Optional.of(bid),
                PING_PONG_SPORT.findWinnerId(PING_PONG_RULE,
                        PING_PONG_SPORT.calcWonSets(match(a, b).getParticipantIdScore())));
    }

    private MatchInfo match(List<Integer> a, List<Integer> b) {
        assertThat(a.size(), equalTo(b.size()));
        return MatchInfo.builder()
                .participantIdScore(ImmutableMap.of(BID_A, a, BID_B, b))
                .build();
    }

    @Test
    public void findWinnerId3To1() {
        assertEquals(Optional.of(new Bid(111)),
                PING_PONG_SPORT.findWinnerId(PING_PONG_RULE, ImmutableMap.of(
                        new Bid(111), 3, new Bid(222), 1)));
    }

    @Test
    public void expandCompositeScore() {
        final Mid mid = Mid.of(3);
        final Tid tid = Tid.of(2);
        assertThat(
                PING_PONG_SPORT.expandScoreSet(
                        PING_PONG_RULE,
                        SetScoreReq
                                .builder()
                                .tid(tid)
                                .setOrdNumber(0)
                                .mid(mid)
                                .scores(asList(scoreOf(BID_A, 1), scoreOf(BID_B, 3)))
                                .build()),
                hasItems(
                        allOf(
                                hasProperty("tid", is(tid)),
                                hasProperty("mid", is(mid)),
                                hasProperty("setOrdNumber", is(0)),
                                hasProperty("scores", hasItems(
                                        allOf(
                                                hasProperty("bid", is(BID_A)),
                                                hasProperty("score", is(PING_PONG_RULE.getMinGamesToWin()))),
                                        allOf(
                                                hasProperty("bid", is(BID_B)),
                                                hasProperty("score", is(PING_PONG_RULE.getMinPossibleGames())))))),
                        allOf(
                                hasProperty("setOrdNumber", is(1)),
                                hasProperty("scores", hasItems(
                                        allOf(
                                                hasProperty("bid", is(BID_A)),
                                                hasProperty("score", is(PING_PONG_RULE.getMinPossibleGames()))),
                                        allOf(
                                                hasProperty("bid", is(BID_B)),
                                                hasProperty("score", is(PING_PONG_RULE.getMinGamesToWin()))))))));
    }
}
