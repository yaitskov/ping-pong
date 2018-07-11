package org.dan.ping.pong.app.sport.tennis;

import static java.lang.Math.max;
import static org.dan.ping.pong.app.sport.SportType.Tennis;
import static org.dan.ping.pong.app.sport.tennis.CanonicalSetValidator.A;
import static org.dan.ping.pong.app.sport.tennis.CanonicalSetValidator.B;
import static org.dan.ping.pong.app.sport.tennis.CanonicalSetValidator.GAMES;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import org.dan.ping.pong.app.bid.Bid;
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
    public static final String TO_MANY_SETS = "to many sets";
    public static final String DIFFERENCE_BETWEEN_GAMES_CANNOT_BE_MORE_THAN = "Difference between games cannot be more than";
    public static final String GAMES_CANNOT_BE_EQUAL = "Games cannot be equal";
    public static final String SET_LENGTH_MISMATCH = "set length mismatch";
    public static final String WON_SETS_MORE_THAT_REQUIRED = "won sets more that required";
    public static final String WINNERS_ARE_MORE_THAT_1 = "winners are more that 1";

    @Override
    public void validate(Multimap<String, ValidationError> errors, TennisMatchRules rules) {
        TennisMatchRuleValidator.validate(errors, rules);
    }

    @Override
    public void validate(TennisMatchRules rules, MatchInfo match) {
        final Map<Bid, List<Integer>> scores = match.getParticipantIdScore();
        final Iterator<Bid> uidIterator = scores.keySet().iterator();
        final Bid bidA = uidIterator.next();
        final Bid bidB = uidIterator.next();
        validateLength(rules, scores, bidA, bidB);
        final int[] wonSets = new int[2];
        for (int iSet = 0; iSet < scores.get(bidA).size(); ++iSet) {
            if (wonSets[A] == wonSets[B]
                    && iSet == rules.getSetsToWin()
                    && rules.getSuperTieBreakGames().isPresent()) {
                CanonicalSetValidator.validate(
                        new SuperTieBreakAdapter(rules),
                        iSet, scores.get(bidA).get(iSet),
                        scores.get(bidB).get(iSet));
            } else {
                wonSets[validateUsualSet(rules, iSet,
                        scores.get(bidA).get(iSet),
                        scores.get(bidB).get(iSet))]++;
            }
        }
    }

    private void validateLength(TennisMatchRules rules,
            Map<Bid, List<Integer>> scores,
            Bid bidA, Bid bidB) {
        validateLength(scores.get(bidA), rules.getSetsToWin());
        validateLength(scores.get(bidB), rules.getSetsToWin());
        if (scores.get(bidA).size() != scores.get(bidB).size()) {
            throw badRequest(SET_LENGTH_MISMATCH);
        }
    }

    private void validateLength(List<Integer> l, int setsToWin) {
        if (l.size() > (setsToWin - 1) * 2 + 1) {
            throw badRequest(TO_MANY_SETS);
        }
    }

    private int validateUsualSet(TennisMatchRules rules, int iSet, int aGames, int bGames) {
        final int maxGames = max(aGames, bGames);
        final int minGames = Math.min(aGames, bGames);
        if (minGames == maxGames) {
            throw badRequest(GAMES_CANNOT_BE_EQUAL, CanonicalSetValidator.SET, iSet);
        }
        if (minGames < rules.getMinPossibleGames()) {
            throw badRequest(TennisMatchRules.GAMES_CANNOT_BE_LESS_THAN,
                    ImmutableMap.of(CanonicalSetValidator.SET, iSet,
                            GAMES, rules.getMinPossibleGames()));
        }
        if (maxGames < rules.getMinGamesToWin()) {
            throw badRequest(SuperTieBreakAdapter.WINNER_SHOULD_HAVE_AT_LEAST_N_GAMES,
                    ImmutableMap.of(CanonicalSetValidator.SET, iSet,
                            GAMES, rules.getMinGamesToWin()));
        }
        if (maxGames == rules.getMinGamesToWin()) {
            if (maxGames - minGames < rules.getMinAdvanceInGames()) {
                throw badRequest(TennisMatchRules.DIFFERENCE_BETWEEN_GAMES_CANNOT_BE_LESS_THAN,
                        ImmutableMap.of(CanonicalSetValidator.SET, iSet,
                                "minAdvanceInGames", rules.getMinAdvanceInGames()));
            }
        } else if (maxGames == rules.getMinGamesToWin() + 1) {
            if (maxGames - minGames > rules.getMinAdvanceInGames()) {
                throw badRequest(DIFFERENCE_BETWEEN_GAMES_CANNOT_BE_MORE_THAN,
                        ImmutableMap.of(CanonicalSetValidator.SET, iSet,
                                "minAdvanceInGames", rules.getMinAdvanceInGames()));
            }
        } else {
            throw badRequest(TennisMatchRules.WINNER_HAS_TO_MUCH_GAMES, CanonicalSetValidator.SET, iSet);
        }
        if (aGames < bGames) {
            return B;
        }
        return A;
    }

    @Override
    public SportType getType() {
        return Tennis;
    }

    public Optional<Bid> findWinnerId(TennisMatchRules rules, Map<Bid, Integer> wonSets) {
        return wonSets.entrySet().stream()
                .filter(e -> e.getValue() >= rules.getSetsToWin())
                .map(Map.Entry::getKey)
                .findAny();
    }

    public void checkWonSets(TennisMatchRules rules, Map<Bid, Integer> bidWonSets) {
        final Collection<Integer> wonSets = bidWonSets.values();
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

    @Override
    public int maxUpLimitSetsDiff(TennisMatchRules rules) {
        return max(2, rules.getSetsToWin());
    }

    @Override
    public int maxUpLimitBallsDiff(TennisMatchRules rules) {
        return max(2, rules.getSuperTieBreakGames().orElseGet(rules::getMinGamesToWin)
                + max(rules.getMinGamesToWin(), rules.getMinAdvanceInGames())
                * (2 * rules.getSetsToWin() - 1));
    }
}
