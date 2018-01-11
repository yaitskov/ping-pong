class PingPongScoresStrategy {
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

class TennisScoresStrategy {
    showExtend() {
        return false;
    }
    isSuperTieBreak(rules, playedSets) {
        return playedSets == rules.setsToWin;
    }
    winnerOptions(rules, playedSets) {
        if (this.isSuperTieBreak(rules, playedSets)) {
            return [rules.superTieBreakGames];
        }
        return [rules.minGamesToWin, rules.minGamesToWin + 1];
    }
    loserOptions(rules, winnerOption, playedSets) {
        if (this.isSuperTieBreak(rules, playedSets)) {
           let result = [];
           for (let i of rules.superTieBreakGames) {
               result.push(i);
           }
           return result;
        }
        if (winnerOption == rules.minGamesToWin) {
           let result = [];
           for (let i = 0; i <= (winnerOption - rules.minAdvanceInGames); ++i) {
               result.push(i);
           }
           return result;
        }
        if (winnerOption == rules.minGamesToWin + 1) {
           let i = winnerOption - rules.minAdvanceInGames;
           let result = [];
           while (i < winnerOption) {
               result.push(i++);
           }
           return result;
        }
    }
    defaultWinnerScore(rules, playedSets) {
        if (this.isSuperTieBreak(rules, playedSets)) {
           return rules.superTieBreakGames;
        }
        return rules.minGamesToWin;
    }
}


const possibleScoresStrategies = {
    Tennis: new TennisScoresStrategy(),
    PingPong: new PingPongScoresStrategy()
};

export default possibleScoresStrategies;
