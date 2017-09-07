package org.dan.ping.pong.app.tournament;

import static com.google.common.primitives.Ints.asList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import org.dan.ping.pong.app.match.IdentifiedScore;
import org.dan.ping.pong.app.match.MatchInfo;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class MatchValidationRuleUnitTest {
    public static final MatchValidationRule PING_PONG_RULE = MatchValidationRule.builder()
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
    private static final int UID_A = 1;
    private static final int UID_B = 2;


    @Test
    public void validatePass() {
        PING_PONG_RULE.validateSet(scores(11, 0));
        PING_PONG_RULE.validateSet(scores(0, 11));
        PING_PONG_RULE.validateSet(scores(9, 11));
        PING_PONG_RULE.validateSet(scores(11, 9));
        PING_PONG_RULE.validateSet(scores(15, 13));
        TENNIS_RULE.validateSet(scores(6, 4));
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
        findWinner(UID_A, asList(11, 11, 11), asList(0, 1, 2));
    }

    @Test
    public void uidBBarelyWins() {
        findWinner(UID_B, asList(11, 11, 9, 14, 12), asList(0, 0, 11, 16, 14));
    }

    private void cannotFindWinner(List<Integer> a, List<Integer> b) {
        assertEquals(Optional.empty(),
                PING_PONG_RULE.findWinnerId(PING_PONG_RULE.calcWonSets(match(a, b))));
    }

    private void findWinner(int uid, List<Integer> a, List<Integer> b) {
        assertEquals(Optional.of(uid),
                PING_PONG_RULE.findWinnerId(PING_PONG_RULE.calcWonSets(match(a, b))));
    }

    private MatchInfo match(List<Integer> a, List<Integer> b) {
        assertThat(a.size(), equalTo(b.size()));
        return MatchInfo.builder()
                .participantIdScore(ImmutableMap.of(UID_A, a, UID_B, b))
                .build();
    }
}
