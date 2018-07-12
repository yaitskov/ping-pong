import SimpleController from 'core/angular/SimpleController.js';

export default class MatchScoreEditorCtrl extends SimpleController {
    static get $inject () {
        return ['Match', 'mainMenu', '$routeParams', 'AjaxInfo',
                'binder', '$rootScope', '$scope', '$location'];
    }

    $onInit() {
        this.ajax = this.AjaxInfo.scope(this.$scope);
        this.mainMenu.setTitle('Match score editor'),
        this.tournamentId = this.$routeParams.tournamentId;
        this.matchId = this.$routeParams.matchId;
        this.setRescoring = false;
        this.match = null;

        this.binder(this.$scope, {
            'event.match.set.scored': (event, setScore) => {
                this.setRescoring = false;
                this.mergeSetScore(setScore);
            },
            'event.base.match.set.pick.lost': (e, setScore) => {
                this.setRescoring = false;
                this.mergeSetScore(setScore);
            },
            'event.review.match.set.appended': (event) => {
                this.setRescoring = true;
                this.match.setScores = null;
                const keys = Object.keys(this.match.score.sets);
                for (let key of keys) {
                    this.match.playedSets = this.match.score.sets[key].length;
                    break;
                }
                this.$rootScope.$broadcast('event.match.set', this.match);
            },
            'event.review.match.set.popped': (event) => {
                this.removeLastSet();
            },
            'event.review.match.set.picked': (event, setIdx, set) => {
                this.setRescoring = true;
                this.match.setScores = [set.a, set.b];
                this.match.playedSets = setIdx;
                this.$rootScope.$broadcast('event.match.set', this.match);
            },
            'event.review.match.ready': (event) => {
                this.$rootScope.$broadcast('event.review.match.config', {edit: true});
                this.ajax.doAjax(
                    '',
                    this.Match.matchResult,
                    {tournamentId: this.$routeParams.tournamentId,
                     matchId: this.$routeParams.matchId},
                    (match) => {
                        this.match = match;
                        this.$rootScope.$broadcast('event.review.match.data', match);
                    });
            }
        });
    }

    rescoreMatch() {
        this.ajax.doAjax(
            '',
            this.Match.rescoreMatch,
            {tid: this.match.tid,
             mid: this.match.score.mid,
             effectHash: this.effectHash,
             sets: this.match.score.sets},
            (ok) => {

                this.$location.path('/match/management/' +
                               this.$routeParams.tournamentId + '/' +
                               this.$routeParams.matchId);
            },
            (errRes, defaultCb) => {
                if (errRes.status == 400 && errRes.data.error == 'effectHashMismatch') {
                    this.effectHash = errRes.data.effectHash;
                    this.effect = errRes.data.matchesToBeReset;
                } else {
                    defaultCb();
                }
            });
    }

    acceptSetScore() {
        this.$rootScope.$broadcast('event.match.set.score');
    }

    cancelSetScore() {
        this.setRescoring = true;
    }

    mergeSetScore(setScore) {
        setScore.scores.forEach(score => {
            this.match.score.sets[score.bid][setScore.setOrdNumber] = score.score;
        });
        this.$rootScope.$broadcast('event.review.match.data', this.match);
    }

    removeLastSet() {
        const keys = Object.keys(this.match.score.sets);
        for (let key of keys) {
            this.match.score.sets[key].splice(-1, 1);
            this.match.playedSets = this.match.score.sets[key].length;
        }
        this.$rootScope.$broadcast('event.review.match.data', this.match);
    }
}
