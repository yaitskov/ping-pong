export default class PingPongSetsStrategy {
    scoreButtonLabel() {
        return 'score match lbl';
    }

    showExtend() {
        return false;
    }

    winnerOptions(rules, winScore, playedSets) {
        return [rules.setsToWin];
    }

    loserOptions(rules, winnerOption) {
        const result = [];
        for (let i = 0; i < rules.setsToWin; ++i)  {
            result.push(i);
        }
        return result;
    }

    defaultWinnerScore(rules, playedSets) {
        return rules.setsToWin;
    }
}
