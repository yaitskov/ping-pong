import BaseScoresStrategy from './BaseScoresStrategy.js';

export default class PingPongScoresStrategy extends BaseScoresStrategy {
    showExtend() {
        return true;
    }
    winnerOptions(rules, winScore, playedSets) {
        if (winScore == rules.minGamesToWin) {
            return [rules.minGamesToWin];
        } else if (winScore > rules.minGamesToWin) {
            let base = winScore - (winScore % 3);
            return [base - 1, base, base + 1, base + 2];
        } else {
            throw new RangeError('win score ' + winScore + ' is too small');
        }
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
