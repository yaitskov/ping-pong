import SimpleController from 'core/angular/SimpleController.js';

export default class EnlistOfflineCtrl extends SimpleController {
    static get $inject () {
        return ['Match', 'AjaxInfo', '$scope', '$rootScope', 'binder', '$routeParams'];
    }

    $onInit() {
        this.ajax = this.AjaxInfo.scope(this.$scope);

        this.setScoreBtn = 'Score';
        this.tournamentId = this.$routeParams.tournamentId;
        this.matchId = this.$routeParams.matchId;

        this.binder(this.$scope, {
            'event.base.match.set.ready': (event) => {
                this.$rootScope.$broadcast('event.match.set.ready');
            },
            'event.match.set.playedSets': (event, btnLabel, n) => {
                this.setScoreBtn = btnLabel;
                this.nextSetNumber = n;
            },
            'event.match.set.scored': (event, matchScore) => {
                this.ajax.doAjax(
                    'Documenting the score',
                    this.Match.scoreMatch,
                    matchScore,
                    (okResp) => {
                        if (okResp.scoreOutcome == 'MatchComplete' || okResp.scoreOutcome == 'LastMatchComplete') {
                            this.$rootScope.$broadcast('event.match.scored', okResp.matchScore);
                        } else if (okResp.scoreOutcome == 'MatchContinues') {
                            this.$rootScope.$broadcast('event.match.set.next', okResp);
                        } else {
                            this.ajax.scope.transError("Match score response unknown", {name: okResp.scoreOutcome});
                        }
                    },
                    (resp, defaultCb) => {
                        if (resp.status == 400) {
                            if (resp.data.error == 'matchScored') {
                                this.$rootScope.$broadcast('event.match.score.raise.conflict', resp);
                            } else {
                                defaultCb();
                            }
                        } else {
                            defaultCb();
                        }
                    });
            }
        });
    }

    scoreMatchSet() {
        this.$rootScope.$broadcast('event.match.set.score');
    }
}
