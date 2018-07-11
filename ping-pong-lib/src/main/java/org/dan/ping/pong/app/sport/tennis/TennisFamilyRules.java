package org.dan.ping.pong.app.sport.tennis;

public interface TennisFamilyRules {
    int getMinGamesToWin();
    int getMinAdvanceInGames();
    int getMinPossibleGames();
    int getSetsToWin();

    String errorGamesCannotBeLessThan();
    String errorWinnerShouldHaveAtLeast();
    String errorDifferenceBetweenGamesCannotBeLessThan();
    String errorWinnerHasToMuchGames();
}
