import BaseScoresStrategy from './BaseScoresStrategy.js';

export default class PingPongScoresStrategy extends BaseScoresStrategy {
    showExtend(rules, playedSets) {
        return true;
    }

    winnerOptions(rules, winScore, playedSets) {
        return this.winnerOptionsDefault(rules, winScore, playedSets, rules.mgtw);
    }

    winnerOptionsDefault(rules, winScore, playedSets, defaultWin) {
        if (winScore == defaultWin) {
            return [defaultWin];
        } else if (winScore > defaultWin) {
            let base = winScore; // - (winScore % 3);
            return [base - 1, base, base + 1, base + 2];
        } else {
            throw new RangeError('win score ' + winScore + ' is too small');
        }
    }

    loserOptions(rules, winnerOption) {
        return this.loserOptionsDefault(rules, winnerOption, rules.mgtw);
    }

    loserOptionsDefault(rules, winnerOption, defautWin) {
        if (winnerOption == defautWin) {
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
