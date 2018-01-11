package org.dan.ping.pong.app.tournament;

import static com.google.common.primitives.Ints.asList;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.match.MatchInfo;
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

    private static final Uid UID_A = new Uid(1);
    private static final Uid UID_B = new Uid(2);
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
                containsString("Winner should have at least")));
        validate(10, 0);
    }

    @Test
    public void validateFailsOnNegaiteveGames() {
        thrown.expect(hasProperty("message",
                containsString("Games cannot be less than")));
        validate(10, -1);
    }

    @Test
    public void validateFailsOnCloseGames() {
        thrown.expect(hasProperty("message",
                containsString("Difference between games cannot be less than")));
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
        findWinner(UID_A, asList(11, 11, 11), asList(0, 1, 2));
    }

    @Test
    public void uidBBarelyWins() {
        findWinner(UID_B, asList(11, 11, 9, 14, 12), asList(0, 0, 11, 16, 14));
    }

    private void cannotFindWinner(List<Integer> a, List<Integer> b) {
        assertEquals(Optional.empty(),
                PING_PONG_SPORT.findWinnerId(PING_PONG_RULE,
                        PING_PONG_SPORT.calcWonSets(match(a, b).getParticipantIdScore())));
    }

    private void findWinner(Uid uid, List<Integer> a, List<Integer> b) {
        assertEquals(Optional.of(uid),
                PING_PONG_SPORT.findWinnerId(PING_PONG_RULE,
                        PING_PONG_SPORT.calcWonSets(match(a, b).getParticipantIdScore())));
    }

    private MatchInfo match(List<Integer> a, List<Integer> b) {
        assertThat(a.size(), equalTo(b.size()));
        return MatchInfo.builder()
                .participantIdScore(ImmutableMap.of(UID_A, a, UID_B, b))
                .build();
    }

    @Test
    public void findWinnerId3To1() {
        assertEquals(Optional.of(new Uid(111)),
                PING_PONG_SPORT.findWinnerId(PING_PONG_RULE, ImmutableMap.of(
                        new Uid(111), 3, new Uid(222), 1)));
    }
}