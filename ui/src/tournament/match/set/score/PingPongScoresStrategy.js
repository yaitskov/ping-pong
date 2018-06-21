import BaseScoresStrategy from './BaseScoresStrategy.js';

export default class PingPongScoresStrategy extends BaseScoresStrategy {
    showExtend() {
        return true;
    }
    winnerOptions(rules, winScore, playedSets) {
        if (winScore == rules.mgtw) {
            return [rules.mgtw];
        } else if (winScore > rules.mgtw) {
            let base = winScore - (winScore % 3);
            return [base - 1, base, base + 1, base + 2];
        } else {
            throw new RangeError('win score ' + winScore + ' is too small');
        }
    }
    loserOptions(rules, winnerOption) {
        if (winnerOption == rules.mgtw) {
           let result = [];
           for (let i = 0; i <= (winnerOption - rules.maig); ++i) {
               result.push(i);
           }
           return result;
        }
        return [winnerOption - rules.maig];
    }
    defaultWinnerScore(rules, playedSets) {
        return rules.mgtw;
    }
}
