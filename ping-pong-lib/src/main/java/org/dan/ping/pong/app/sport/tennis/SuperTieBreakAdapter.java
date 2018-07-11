package org.dan.ping.pong.app.sport.tennis;

import static org.dan.ping.pong.app.sport.tennis.TennisMatchRules.GAMES_CANNOT_BE_LESS_THAN;
import static org.dan.ping.pong.app.sport.tennis.TennisMatchRules.DIFFERENCE_BETWEEN_GAMES_CANNOT_BE_LESS_THAN;
import static org.dan.ping.pong.app.sport.tennis.TennisMatchRules.WINNER_HAS_TO_MUCH_GAMES;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SuperTieBreakAdapter implements TennisFamilyRules {
    public static final String WINNER_SHOULD_HAVE_AT_LEAST_N_GAMES = "Winner should have at least n games";

    private final TennisMatchRules rules;

    @Override
    public int getMinGamesToWin() {
        return rules.getSuperTieBreakGames().get();
    }

    @Override
    public int getMinAdvanceInGames() {
        return rules.getMinAdvanceInGames();
    }

    @Override
    public int getMinPossibleGames() {
        return rules.getMinPossibleGames();
    }

    @Override
    public int getSetsToWin() {
        return rules.getMinPossibleGames();
    }

    @Override
    public String errorGamesCannotBeLessThan() {
        return GAMES_CANNOT_BE_LESS_THAN;
    }

    @Override
    public String errorWinnerShouldHaveAtLeast() {
        return WINNER_SHOULD_HAVE_AT_LEAST_N_GAMES;
    }

    @Override
    public String errorDifferenceBetweenGamesCannotBeLessThan() {
        return DIFFERENCE_BETWEEN_GAMES_CANNOT_BE_LESS_THAN;
    }

    @Override
    public String errorWinnerHasToMuchGames() {
        return WINNER_HAS_TO_MUCH_GAMES;
    }
}
