package org.dan.ping.pong.app.sport.tennis;

import static org.dan.ping.pong.app.sport.SportType.Tennis;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import org.dan.ping.pong.app.bid.Uid;
import org.dan.ping.pong.app.match.MatchInfo;
import org.dan.ping.pong.app.sport.Sport;
import org.dan.ping.pong.app.sport.SportType;
import org.dan.ping.pong.app.tournament.rules.TennisMatchRuleValidator;
import org.dan.ping.pong.app.tournament.rules.ValidationError;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TennisSport implements Sport<TennisMatchRules> {
    public static final String BALLS = "balls";
    public static final String GAMES_CANNOT_BE_LESS_THAN = "Games cannot be less than";
    public static final String WINNER_SHOULD_HAVE_AT_LEAST_N_GAMES = "Winner should have at least n games";
    public static final String TO_MANY_SETS = "to many sets";
    public static final String WINNER_HAS_TO_MUCH_GAMES = "Winner has to much games";
    public static final String DIFFERENCE_BETWEEN_GAMES_CANNOT_BE_LESS_THAN = "Difference between games cannot be less than";
    public static final String DIFFERENCE_BETWEEN_GAMES_CANNOT_BE_MORE_THAN = "Difference between games cannot be more than";
    public static final String GAMES_CANNOT_BE_EQUAL = "Games cannot be equal";
    public static final String WINNER_SHOULD_GET_N_BALLS_IN_SUPER_TIEBREAK = "Winner should get n balls in super tiebreak";
    public static final String SET_LENGTH_MISMATCH = "set length mismatch";
    public static final String WON_SETS_MORE_THAT_REQUIRED = "won sets more that required";
    public static final String WINNERS_ARE_MORE_THAT_1 = "winners are more that 1";
    private static int PlayerA = 1;
    private static int PlayerB = 0;

    private static final String SET = "set";
    private static final String MIN_POSSIBLE_GAMES = "minPossibleGames";
    private static final String MIN_GAMES_TO_WIN = "minGamesToWin";

    @Override
    public void validate(Multimap<String, ValidationError> errors, TennisMatchRules rules) {
        TennisMatchRuleValidator.validate(errors, rules);
    }

    @Override
    public void validate(TennisMatchRules rules, MatchInfo match) {
        final Map<Uid, List<Integer>> scores = match.getParticipantIdScore();
        final Iterator<Uid> uidIterator = scores.keySet().iterator();
        final Uid uidA = uidIterator.next();
        final Uid uidB = uidIterator.next();
        validateLength(rules, scores, uidA, uidB);
        final int[] wonSets = new int[2];
        for (int iSet = 0; iSet < scores.get(uidA).size(); ++iSet) {
            if (wonSets[PlayerA] == wonSets[PlayerB] && iSet == rules.getSetsToWin()) {
                validateSuperTieBreakSet(rules, iSet, scores.get(uidA).get(iSet),
                        scores.get(uidB).get(iSet));
            } else {
                wonSets[validateUsualSet(rules, iSet,
                        scores.get(uidA).get(iSet),
                        scores.get(uidB).get(iSet))]++;
            }
        }
    }

    private void validateLength(TennisMatchRules rules,
            Map<Uid, List<Integer>> scores,
            Uid uidA, Uid uidB) {
        validateLength(scores.get(uidA), rules.getSetsToWin());
        validateLength(scores.get(uidB), rules.getSetsToWin());
        if (scores.get(uidA).size() != scores.get(uidB).size()) {
            throw badRequest(SET_LENGTH_MISMATCH);
        }
    }

    private void validateLength(List<Integer> l, int setsToWin) {
        if (l.size() > (setsToWin - 1) * 2 + 1) {
            throw badRequest(TO_MANY_SETS);
        }
    }

    private int validateUsualSet(TennisMatchRules rules, int iSet, int aGames, int bGames) {
        final int maxGames = Math.max(aGames, bGames);
        final int minGames = Math.min(aGames, bGames);
        if (minGames == maxGames) {
            throw badRequest(GAMES_CANNOT_BE_EQUAL, SET, iSet);
        }
        if (minGames < rules.getMinPossibleGames()) {
            throw badRequest(GAMES_CANNOT_BE_LESS_THAN,
                    ImmutableMap.of(SET, iSet,
                            MIN_POSSIBLE_GAMES, rules.getMinPossibleGames()));
        }
        if (maxGames < rules.getMinGamesToWin()) {
            throw badRequest(WINNER_SHOULD_HAVE_AT_LEAST_N_GAMES,
                    ImmutableMap.of(SET, iSet,
                            MIN_GAMES_TO_WIN, rules.getMinGamesToWin()));
        }
        if (maxGames == rules.getMinGamesToWin()) {
            if (maxGames - minGames < rules.getMinAdvanceInGames()) {
                throw badRequest(DIFFERENCE_BETWEEN_GAMES_CANNOT_BE_LESS_THAN,
                        ImmutableMap.of(SET, iSet,
                                "minAdvanceInGames", rules.getMinAdvanceInGames()));
            }
        } else if (maxGames == rules.getMinGamesToWin() + 1) {
            if (maxGames - minGames > rules.getMinAdvanceInGames()) {
                throw badRequest(DIFFERENCE_BETWEEN_GAMES_CANNOT_BE_MORE_THAN,
                        ImmutableMap.of(SET, iSet,
                                "minAdvanceInGames", rules.getMinAdvanceInGames()));
            }
        } else {
            throw badRequest(WINNER_HAS_TO_MUCH_GAMES, SET, iSet);
        }
        if (aGames < bGames) {
            return PlayerB;
        }
        return PlayerA;
    }

    private void validateSuperTieBreakSet(TennisMatchRules rules, int iSet, int aGames, int bGames) {
        final int maxGames = Math.max(aGames, bGames);
        final int minGames = Math.min(aGames, bGames);
        if (minGames < rules.getMinPossibleGames()) {
            throw badRequest(GAMES_CANNOT_BE_LESS_THAN,
                    ImmutableMap.of(SET, iSet,
                            MIN_POSSIBLE_GAMES, rules.getMinPossibleGames()));
        }
        if (minGames == maxGames) {
            throw badRequest(GAMES_CANNOT_BE_EQUAL, SET, iSet);
        }
        if (maxGames != rules.getSuperTieBreakGames()) {
            throw badRequest(WINNER_SHOULD_GET_N_BALLS_IN_SUPER_TIEBREAK,
                    ImmutableMap.of(SET, iSet,
                            BALLS, rules.getSuperTieBreakGames()));
        }
    }

    @Override
    public SportType getType() {
        return Tennis;
    }

    public Optional<Uid> findWinnerId(TennisMatchRules rules, Map<Uid, Integer> wonSets) {
        return wonSets.entrySet().stream()
                .filter(e -> e.getValue() >= rules.getSetsToWin())
                .map(Map.Entry::getKey)
                .findAny();
    }

    public void checkWonSets(TennisMatchRules rules, Map<Uid, Integer> uidWonSets) {
        final Collection<Integer> wonSets = uidWonSets.values();
        wonSets.stream()
                .filter(n -> n > rules.getSetsToWin()).findAny()
                .ifPresent(o -> {
                    throw badRequest(WON_SETS_MORE_THAT_REQUIRED);
                });
        final long winners = wonSets.stream()
                .filter(n -> n == rules.getSetsToWin())
                .count();
        if (winners > 1) {
            throw badRequest(WINNERS_ARE_MORE_THAT_1);
        }
    }
}
