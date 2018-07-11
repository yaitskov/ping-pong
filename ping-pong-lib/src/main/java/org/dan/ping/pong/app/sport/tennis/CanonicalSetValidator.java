package org.dan.ping.pong.app.sport.tennis;

import static java.lang.Math.max;
import static org.dan.ping.pong.sys.error.PiPoEx.badRequest;

import com.google.common.collect.ImmutableMap;

public class CanonicalSetValidator {
    public static final int A = 0;
    public static final int B = 1;
    public static final String GAMES = "games";
    public static final String SET = "set";

    public static int validate(TennisFamilyRules rules, int iSet, int aGames, int bGames) {
        final int maxGames = max(aGames, bGames);
        final int minGames = Math.min(aGames, bGames);
        if (minGames < rules.getMinPossibleGames()) {
            throw badRequest(rules.errorGamesCannotBeLessThan(),
                    ImmutableMap.of(SET, iSet,
                            GAMES, rules.getMinPossibleGames()));
        }
        if (maxGames < rules.getMinGamesToWin()) {
            throw badRequest(rules.errorWinnerShouldHaveAtLeast(),
                    ImmutableMap.of(SET, iSet,
                            GAMES, rules.getMinGamesToWin()));
        }
        if (maxGames - minGames < rules.getMinAdvanceInGames()) {
            throw badRequest(rules.errorDifferenceBetweenGamesCannotBeLessThan(),
                    ImmutableMap.of(SET, iSet,
                            GAMES, rules.getMinAdvanceInGames()));
        }
        if (maxGames > rules.getMinGamesToWin()
                && maxGames - minGames > rules.getMinAdvanceInGames()) {
            throw badRequest(rules.errorWinnerHasToMuchGames(), SET, iSet);
        }
        if (aGames < bGames) {
            return B;
        }
        return A;
    }
}
