package org.dan.ping.pong.app.sport.tennis;

import static com.google.common.primitives.Ints.asList;
import static java.util.Collections.emptyList;
import static org.dan.ping.pong.app.sport.tennis.TennisSport.DIFFERENCE_BETWEEN_GAMES_CANNOT_BE_LESS_THAN;
import static org.dan.ping.pong.app.sport.tennis.TennisSport.DIFFERENCE_BETWEEN_GAMES_CANNOT_BE_MORE_THAN;
import static org.dan.ping.pong.app.sport.tennis.TennisSport.GAMES_CANNOT_BE_EQUAL;
import static org.dan.ping.pong.app.sport.tennis.TennisSport.GAMES_CANNOT_BE_LESS_THAN;
import static org.dan.ping.pong.app.sport.tennis.TennisSport.SET_LENGTH_MISMATCH;
import static org.dan.ping.pong.app.sport.tennis.TennisSport.TO_MANY_SETS;
import static org.dan.ping.pong.app.sport.tennis.TennisSport.WINNER_HAS_TO_MUCH_GAMES;
import static org.dan.ping.pong.app.sport.tennis.TennisSport.WINNER_SHOULD_GET_N_BALLS_IN_SUPER_TIEBREAK;
import static org.dan.ping.pong.app.sport.tennis.TennisSport.WINNER_SHOULD_HAVE_AT_LEAST_N_GAMES;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasProperty;

import com.google.common.collect.ImmutableMap;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.match.MatchInfo;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;

public class TennisSportTest {
    private TennisSport sut = new TennisSport();
    public static final  TennisMatchRules CLASSIC_TENNIS_RULES = TennisMatchRules.builder()
            .setsToWin(2)
            .minGamesToWin(6)
            .minAdvanceInGames(2)
            .superTieBreakGames(10)
            .build();

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
        validateClassic(asList(7, 0, 9), asList(6, 6, 10));
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
        expect(WINNER_SHOULD_GET_N_BALLS_IN_SUPER_TIEBREAK);
        validateClassic(asList(6, 4, 6), asList(4, 6, 4));
    }

    @Test
    public void validateFailToMatchForSuperTieBreak() {
        expect(WINNER_SHOULD_GET_N_BALLS_IN_SUPER_TIEBREAK);
        validateClassic(asList(6, 4, 11), asList(4, 6, 4));
    }

    @Test
    public void validateFailToMatchForNonTieBreak() {
        expect(WINNER_HAS_TO_MUCH_GAMES);
        validateClassic(asList(6, 11), asList(4, 6));
    }

    private void expect(String error) {
        thrown.expect(hasProperty("message", containsString(error)));
    }

    private void validateClassic(List<Integer> a, List<Integer> b) {
        sut.validate(CLASSIC_TENNIS_RULES, match(a, b));
    }

    private MatchInfo match(List<Integer> a, List<Integer> b) {
        return MatchInfo.builder()
                .participantIdScore(
                        ImmutableMap.of(new Uid(2), a, new Uid(3), b))
                .build();
    }
}
