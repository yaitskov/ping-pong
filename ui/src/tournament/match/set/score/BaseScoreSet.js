import AngularBean from 'core/angular/AngularBean.js';
import pickScoreStrategy from './pickScoreStrategy.js';

export default class BaseScoreSet extends AngularBean {
    static get $inject() {
        return ['$scope', '$location', 'InfoPopup',
                'syncTranslate', '$rootScope', 'binder', '$routeParams'];
    }

    activate(idx) {
        if (this.winnerIdx != idx) {
            this.scores.reverse();
            this.winnerIdx = idx;
        }
    }

    reset() {
        if (this.match.setScores) {
            this.scores = this.match.setScores.slice();
            let winScore = Math.max.apply(null, this.scores);
            let lostScore = Math.min.apply(null, this.scores);
            this.winnerIdx = (winScore == this.scores[0]) ? 0 : 1;
            this.scores[this.winnerIdx] = winScore;
            this.scores[1 - this.winnerIdx] = lostScore;
            const sport = this.match.sport;
            this.possibleWinScores = this.scoreStrategy.winnerOptions(
                sport, winScore, this.match.playedSets);
            this.possibleLostScores = this.scoreStrategy.loserOptions(
                sport, winScore, this.match.playedSets);
        } else {
            this.scores = [-1, -1];
            this.noBalance();
        }
    }

    noBalance() {
        const sport = this.match.sport;
        const winScore = this.scoreStrategy.defaultWinnerScore(
            sport, this.match.playedSets);
        this.scores[this.winnerIdx] = winScore;
        this.possibleWinScores = this.scoreStrategy.winnerOptions(
            sport, winScore, this.match.playedSets);
        this.possibleLostScores = this.scoreStrategy.loserOptions(
            sport, this.scores[this.winnerIdx], this.match.playedSets);
    }

    showExtend() {
        return this.scoreStrategy && this.scoreStrategy.showExtend(
            this.match.sport, this.match.playedSets);
    }

    extendWinScore() {
        const max = this.possibleWinScores[this.possibleWinScores.length - 1];
        this.pick(this.winnerIdx, max + 1, true);
    }

    isLostToBig(playerIdx) {
        return this.possibleLostScores[this.possibleLostScores.length - 1] < this.scores[playerIdx];
    }

    isLostToSmall(playerIdx) {
        return this.possibleLostScores[0] > this.scores[playerIdx];
    }

    otherPlayer(playerIdx) {
        return 1 - playerIdx;
    }

    resetPlayerScore(playerIdx) {
        this.scores[playerIdx] = -1;
    }

    pick(idx, score, extension) {
        this.scores[idx] = score;
        const sport = this.match.sport;
        this.possibleWinScores = this.scoreStrategy.winnerOptions(sport, score, this.match.playedSets);
        this.possibleLostScores = this.scoreStrategy.loserOptions(sport, score, this.match.playedSets);
        if (this.possibleLostScores.length == 1) {
            this.scores[1 - idx] = this.possibleLostScores[0];
            if (!extension) {
                this.$rootScope.$broadcast('event.base.match.set.pick.lost',
                                           {setOrdNumber: this.match.playedSets,
                                            scores: this.findScores()});
            }
        } else {
            const otherPlayerIdx = this.otherPlayer(idx);
            if (this.isLostToBig(otherPlayerIdx) || this.isLostToSmall(otherPlayerIdx)) {
                this.resetPlayerScore(otherPlayerIdx);
            }
        }
    }

    findScores() {
        return [{bid: this.participants[this.winnerIdx].bid,
                 score: this.scores[this.winnerIdx]},
                {bid: this.participants[1 - this.winnerIdx].bid,
                 score: this.scores[1 - this.winnerIdx]}];
    }

    pickLost(idx, score) {
        this.scores[idx] = score;
        this.$rootScope.$broadcast('event.base.match.set.pick.lost',
                                   {setOrdNumber: this.match.playedSets,
                                    scores: this.findScores()});
    }

    onMatchSet(match) {
        console.log("caught event.match.set");
        this.match = match;
        this.scoreStrategy = pickScoreStrategy(match.sport);
        this.participants = match.participants;
        this.tournamentId = match.tid;
        this.nextScoreUpdated();
        this.reset();
    }

    nextScoreUpdated() {
        const playedSets = 1 + this.match.playedSets;
        this.sBtnTrans.trans(this.scoreStrategy.scoreButtonLabel(playedSets), (v) => {
            this.$rootScope.$broadcast('event.match.set.playedSets', v, playedSets);
            this.setScoreBtn = v;
        });
    }

    scoreIsProvided() {
        if (this.scores[0] < 0 || this.scores[1] < 0) {
            this.info.transError("Not all participants have been scored");
            return;
        }
        if (this.scores[0] == this.scores[1]) {
            this.info.transError("Participants cannot have same scores");
            return;
        }
        this.$rootScope.$broadcast(
            'event.match.set.scored',
            {mid: this.$routeParams.matchId,
             tid: this.tournamentId,
             setOrdNumber: this.match.playedSets,
             scores: this.findScores()});
    }

    nextScore(okResp) {
        this.info.transInfo(
            'Set n scored. Match continues', {n: 1 + this.match.playedSets});
        this.match.playedSets = okResp.nextSetNumberToScore;
        this.nextScoreUpdated();
        this.reset();
    }

    scoreConflict(data) {
        this.$rootScope.$broadcast('event.match.score.conflict',
                                   {matchId: this.$routeParams.matchId,
                                    matchScore: data.data.matchScore,
                                    yourSetScore: this.findScores(),
                                    participants: this.match.participants,
                                    yourSet: this.match.playedSets});
    }

    constructor() {
        super(...arguments);
        this.info = this.InfoPopup.createScope(this.$scope);

        this.sBtnTrans = this.syncTranslate.create();
        this.winnerIdx = 0;

        this.binder(this.$scope, {
            'event.match.set': (event, match) => this.onMatchSet(match),
            'event.match.set.next': (event, okResp) => this.nextScore(okResp),
            'event.match.score.raise.conflict': (event, data) => this.scoreConflict(data),
            'event.match.set.score': () => this.scoreIsProvided()
        });

        this.$rootScope.$broadcast('event.base.match.set.ready');
    }
}
