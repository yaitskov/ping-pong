import BaseScoresStrategy from './BaseScoresStrategy.js';
import PingPongScoresStrategy from './PingPongScoresStrategy.js';

export default class TennisScoresStrategy extends BaseScoresStrategy {
    constructor() {
        super();
        this.pingPong = new PingPongScoresStrategy();
    }

    showExtend(rules, playedSets) {
        return this._isSuperTieBreak(rules, playedSets);
    }

    _isSuperTieBreak(rules, playedSets) {
        return rules.stbg && playedSets > 0 && playedSets == (rules.stw - 1) * 2;
    }

    winnerOptions(rules, winScore, playedSets) {
        if (this._isSuperTieBreak(rules, playedSets)) {
            return this.pingPong.winnerOptionsDefault(rules, winScore, playedSets, rules.stbg);
        }
        return [rules.mgtw, rules.mgtw + 1];
    }

    loserOptions(rules, winnerOption, playedSets) {
        if (this._isSuperTieBreak(rules, playedSets)) {
            return this.pingPong.loserOptionsDefault(rules, winnerOption, rules.stbg);
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
            return rules.stbg;
        }
        return rules.mgtw;
    }
}
