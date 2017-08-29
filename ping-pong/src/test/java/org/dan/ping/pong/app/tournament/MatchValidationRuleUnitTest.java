package org.dan.ping.pong.app.tournament;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasProperty;

import org.dan.ping.pong.app.match.IdentifiedScore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;
import java.util.stream.Stream;

public class MatchValidationRuleUnitTest {
    private static final MatchValidationRule PING_PONG_RULE = MatchValidationRule.builder()
            .minGamesToWin(11)
            .minPossibleGames(0)
            .minAdvanceInGames(2)
            .setsToWin(3)
            .build();

    private static final MatchValidationRule TENNIS_RULE = MatchValidationRule.builder()
            .minGamesToWin(6)
            .minPossibleGames(0)
            .minAdvanceInGames(2)
            .setsToWin(2)
            .build();


    @Test
    public void validatePass() {
        PING_PONG_RULE.validateSet(scores(11, 0));
        PING_PONG_RULE.validateSet(scores(0, 11));
        PING_PONG_RULE.validateSet(scores(9, 11));
        PING_PONG_RULE.validateSet(scores(11, 9));
        PING_PONG_RULE.validateSet(scores(15, 13));
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void validateFailsOnMaxGamesToSmall() {
        thrown.expect(hasProperty("message",
                containsString("Winner should have at least")));
        PING_PONG_RULE.validateSet(scores(10, 0));
    }

    @Test
    public void validateFailsOnNegaiteveGames() {
        thrown.expect(hasProperty("message",
                containsString("Games cannot less than")));
        PING_PONG_RULE.validateSet(scores(10, -1));
    }

    @Test
    public void validateFailsOnCloseGames() {
        thrown.expect(hasProperty("message",
                containsString("Difference between games in a complete set")));
        PING_PONG_RULE.validateSet(scores(11, 10));
    }

    private List<IdentifiedScore> scores(int scoreA, int scoreB) {
        return Stream.of(scoreA, scoreB)
                .map(s -> IdentifiedScore.builder().uid(1).score(s).build())
                .collect(toList());
    }
}
