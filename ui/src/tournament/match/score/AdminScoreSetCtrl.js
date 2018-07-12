import SimpleController from 'core/angular/SimpleController.js';

export default class AdminScoreSetCtrl extends SimpleController {
    static get $inject () {
        return ['mainMenu', '$routeParams', 'pageCtx', '$scope', '$location',
                '$rootScope', 'binder', 'eBarier', 'Match', 'AjaxInfo'];
    }

    $onInit() {
        this.mainMenu.setTitle('Match Scoring');
        const matchReady = this.eBarier.create(
            ['score.widget.ready', 'match.loaded'],
            (match) => this.$rootScope.$broadcast('event.match.set', this.match = match));

        this.binder(this.$scope, {
            'event.request.status.ready': (e) => {
            },
            'event.match.set.ready': (event) => matchReady.got('score.widget.ready'),
            'event.match.score.conflict': (event, conflict) => this.showConflict(conflict),
            'event.match.scored': (event, matchScore) => {
                this.pageCtx.put('match-score-review-' + this.$routeParams.matchId,
                            {score: matchScore,
                             participants: this.match.participants
                            });
                this.$location.path('/review/admin-scored-match/' + this.match.tid
                                    + '/' + this.$routeParams.matchId);
            }
        });

        this.match = this.pageCtx.get('last-scoring-match');
        if (!this.match || this.match.mid != this.$routeParams.matchId) {
            this.match = null;
            this.ajax = this.AjaxInfo.scope(this.$scope);
            this.ajax.doAjax(
                '',
                this.Match.matchForJudge,
                {tournamentId: this.$routeParams.tournamentId,
                 matchId: this.$routeParams.matchId},
                (match) => matchReady.got('match.loaded', match));
        } else {
            matchReady.got('match.loaded', this.match);
        }
    }

    showConflict(conflict) {
        this.pageCtx.put('match-score-conflict-' + this.$routeParams.matchId, conflict);
        this.$location.path('/match/admin-conflict-review/' + this.match.tid
                            + '/' + this.$routeParams.matchId);
    }
}
