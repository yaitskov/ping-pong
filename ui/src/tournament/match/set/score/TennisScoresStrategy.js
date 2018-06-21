import BaseScoresStrategy from './BaseScoresStrategy.js';

export default class TennisScoresStrategy extends BaseScoresStrategy {
    showExtend() {
        return false;
    }
    _isSuperTieBreak(rules, playedSets) {
        return playedSets > 0 && playedSets == (rules.stw - 1) * 2;
    }
    winnerOptions(rules, winScore, playedSets) {
        if (this._isSuperTieBreak(rules, playedSets)) {
            return [rules.superTieBreakGames];
        }
        return [rules.mgtw, rules.mgtw + 1];
    }
    loserOptions(rules, winnerOption, playedSets) {
        if (this._isSuperTieBreak(rules, playedSets)) {
           let result = [];
           for (let i of rules.superTieBreakGames) {
               result.push(i);
           }
           return result;
        }
        if (winnerOption == rules.mgtw) {
           let result = [];
           for (let i = 0; i <= (winnerOption - rules.maig); ++i) {
               result.push(i);
           }
           return result;
        }
        if (winnerOption == rules.mgtw + 1) {
           let i = winnerOption - rules.maig;
           let result = [];
           while (i < winnerOption) {
               result.push(i++);
           }
           return result;
        }
        throw new RangeError('winner got to much games: ' + winnerOption);
    }
    defaultWinnerScore(rules, playedSets) {
        if (this._isSuperTieBreak(rules, playedSets)) {
           return rules.superTieBreakGames;
        }
        return rules.mgtw;
    }
}
