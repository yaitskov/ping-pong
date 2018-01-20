export default class PingPongScoresStrategy {
    showExtend() {
        return true;
    }
    winnerOptions(rules, playedSets) {
        return [rules.minGamesToWin];
    }
    loserOptions(rules, winnerOption) {
        if (winnerOption == rules.minGamesToWin) {
           let result = [];
           for (let i = 0; i <= (winnerOption - rules.minAdvanceInGames); ++i) {
               result.push(i);
           }
           return result;
        }
        return [winnerOption - rules.minAdvanceInGames];
    }
    defaultWinnerScore(rules, playedSets) {
        return rules.minGamesToWin;
    }
}
