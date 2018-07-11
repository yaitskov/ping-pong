package org.dan.ping.pong.app.sport.tennis;

import static com.google.common.primitives.Ints.asList;
import static java.util.Collections.emptyList;
import static org.dan.ping.pong.app.sport.tennis.SuperTieBreakAdapter.WINNER_SHOULD_HAVE_AT_LEAST_N_GAMES;
import static org.dan.ping.pong.app.sport.tennis.TennisMatchRules.DIFFERENCE_BETWEEN_GAMES_CANNOT_BE_LESS_THAN;
import static org.dan.ping.pong.app.sport.tennis.TennisMatchRules.GAMES_CANNOT_BE_LESS_THAN;
import static org.dan.ping.pong.app.sport.tennis.TennisMatchRules.WINNER_HAS_TO_MUCH_GAMES;
import static org.dan.ping.pong.app.sport.tennis.TennisSport.DIFFERENCE_BETWEEN_GAMES_CANNOT_BE_MORE_THAN;
import static org.dan.ping.pong.app.sport.tennis.TennisSport.GAMES_CANNOT_BE_EQUAL;
import static org.dan.ping.pong.app.sport.tennis.TennisSport.SET_LENGTH_MISMATCH;
import static org.dan.ping.pong.app.sport.tennis.TennisSport.TO_MANY_SETS;
import static org.dan.ping.pong.app.sport.tennis.TennisSport.WINNERS_ARE_MORE_THAT_1;
import static org.dan.ping.pong.app.sport.tennis.TennisSport.WON_SETS_MORE_THAT_REQUIRED;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import org.dan.ping.pong.app.bid.Bid;
import org.dan.ping.pong.app.match.MatchInfo;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;
import java.util.Optional;

public class TennisSportTest {
    private static final Bid BID_A = new Bid(1);
    private static final Bid BID_B = new Bid(2);

    private TennisSport sut = new TennisSport();
    public static final  TennisMatchRules CLASSIC_TENNIS_RULES = TennisMatchRules.builder()
            .setsToWin(2)
            .minGamesToWin(6)
            .minAdvanceInGames(2)
            .superTieBreakGames(Optional.of(10))
            .build();

    public static final TennisMatchRules SHORT_TENNIS_RULES = TennisMatchRules.builder()
            .setsToWin(1)
            .minGamesToWin(6)
            .minAdvanceInGames(2)
            .superTieBreakGames(Optional.of(10))
            .build();

    @Test
    public void validateShortPass() {
        validateShort(emptyList(), emptyList());
        validateShort(asList(0), asList(6));
        validateShort(asList(4), asList(6));
        validateShort(asList(6), asList(1));
        validateShort(asList(7), asList(5));
        validateShort(asList(7), asList(6));
    }

    @Test
    public void validateFailShortToManySets() {
        expect(TO_MANY_SETS);
        validateShort(asList(6, 4), asList(4, 6));
    }

    @Test
    public void validatePass() {
        validateClassic(emptyList(), emptyList());
        validateClassic(asList(0), asList(6));
        validateClassic(asList(4), asList(6));
        validateClassic(asList(6), asList(1));
        validateClassic(asList(7), asList(5));
        validateClassic(asList(7), asList(6));
        validateClassic(asList(7, 7), asList(6, 5));
        validateClassic(asList(7, 4), asList(6, 6));
        validateClassic(asList(7, 0, 8), asList(6, 6, 10));
        validateClassic(asList(7, 3, 0), asList(6, 6, 10));
        validateClassic(asList(7, 3, 6), asList(6, 6, 10));
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void validateFailLessMin() {
        expect(GAMES_CANNOT_BE_LESS_THAN);
        validateClassic(asList(-1), asList(6));
    }

    @Test
    public void validateWinnerGetSmall() {
        expect(WINNER_SHOULD_HAVE_AT_LEAST_N_GAMES);
        validateClassic(asList(0), asList(5));
    }

    @Test
    public void validateFailNoTieBreak() {
        expect(DIFFERENCE_BETWEEN_GAMES_CANNOT_BE_LESS_THAN);
        validateClassic(asList(5), asList(6));
    }

    @Test
    public void validateFailEqual() {
        expect(GAMES_CANNOT_BE_EQUAL);
        validateClassic(asList(7), asList(7));
    }

    @Test
    public void validateFailLoserToSmallForTieBreak() {
        expect(DIFFERENCE_BETWEEN_GAMES_CANNOT_BE_MORE_THAN);
        validateClassic(asList(4), asList(7));
    }

    @Test
    public void validateFailToManySets() {
        expect(TO_MANY_SETS);
        validateClassic(asList(6, 4, 6, 4), asList(4, 6, 4, 6));
    }

    @Test
    public void validateFailLengthMismatch() {
        expect(SET_LENGTH_MISMATCH);
        validateClassic(asList(6, 4), asList(4));
    }

    @Test
    public void validateFailNotEnoughForSuperTieBreak() {
        expect(WINNER_SHOULD_HAVE_AT_LEAST_N_GAMES);
        validateClassic(asList(6, 4, 6), asList(4, 6, 4));
    }

    @Test
    public void validateFailToMatchForSuperTieBreak() {
        expect(WINNER_HAS_TO_MUCH_GAMES);
        validateClassic(asList(6, 4, 11), asList(4, 6, 4));
    }

    @Test
    public void validateFailToMatchForNonTieBreak() {
        expect(WINNER_HAS_TO_MUCH_GAMES);
        validateClassic(asList(6, 11), asList(4, 6));
    }

    @Test
    public void winsUidA() {
        findWinner(Optional.of(BID_A), 2, 1);
    }

    @Test
    public void winsUidB() {
        findWinner(Optional.of(BID_B), 0, 2);
    }

    @Test
    public void nobodyWins() {
        findWinner(Optional.empty(), 1, 1);
    }

    @Test
    public void checkWonSetsPassWithoutWinner() {
        sut.checkWonSets(CLASSIC_TENNIS_RULES, ImmutableMap.of(BID_A, 1, BID_B, 1));
    }

    @Test
    public void checkWonSetsPassWithWinner() {
        sut.checkWonSets(CLASSIC_TENNIS_RULES, ImmutableMap.of(BID_A, 1, BID_B, 2));
        sut.checkWonSets(CLASSIC_TENNIS_RULES, ImmutableMap.of(BID_A, 0, BID_B, 2));
    }

    @Test
    public void checkWonSetsFailWith2Winners() {
        expect(WINNERS_ARE_MORE_THAT_1);
        sut.checkWonSets(CLASSIC_TENNIS_RULES, ImmutableMap.of(BID_A, 2, BID_B, 2));
    }

    @Test
    public void checkWonSetsFailToManySets() {
        expect(WON_SETS_MORE_THAT_REQUIRED);
        sut.checkWonSets(CLASSIC_TENNIS_RULES, ImmutableMap.of(BID_A, 1, BID_B, 3));
    }

    private void findWinner(Optional<Bid> bid, int a, int b) {
        assertEquals(bid, sut.findWinnerId(CLASSIC_TENNIS_RULES, ImmutableMap.of(BID_A, a, BID_B, b)));
    }

    private void expect(String error) {
        thrown.expect(hasProperty("message", containsString(error)));
    }

    private void validateClassic(List<Integer> a, List<Integer> b) {
        sut.validate(CLASSIC_TENNIS_RULES, match(a, b));
    }

    private void validateShort(List<Integer> a, List<Integer> b) {
        sut.validate(SHORT_TENNIS_RULES, match(a, b));
    }

    private MatchInfo match(List<Integer> a, List<Integer> b) {
        return MatchInfo.builder()
                .participantIdScore(
                        ImmutableMap.of(new Bid(2), a, new Bid(3), b))
                .build();
    }
}
