import SimpleController from 'core/angular/SimpleController.js';

export default class ParticipantScoreSetCtrl extends SimpleController {
    static get $inject () {
        return ['auth', 'mainMenu', 'Match', '$routeParams', '$location',
                'pageCtx', '$scope', '$rootScope', 'binder'];
    }

    $onInit() {
        this.mainMenu.setTitle('Match Scoring');
        this.match = this.pageCtx.get('last-scoring-match');
        this.match.participants = [this.match.enemy,
                                   {bid: this.match.bid, name: '*you*'}];

        this.binder(this.$scope, {
            'event.match.set.ready': (event) => this.$rootScope.$broadcast('event.match.set', this.match),
            'event.match.score.conflict': (event, conflict) => this.showConflict(conflict),
            'event.match.scored': (event, matchScore) => {
                this.pageCtx.put('match-score-review-' + this.$routeParams.matchId,
                            {score: matchScore,
                             participants: this.match.participants
                            });
                this.$location.path('/review/user-scored-match/' + this.match.tid
                                    + '/' + this.$routeParams.matchId);
            }
        });
    }

    showConflict(conflict) {
        this.pageCtx.put('match-score-conflict-' + this.$routeParams.matchId, conflict);
        this.$location.path('/match/user-conflict-review/' + this.match.tid
                            + '/' + this.$routeParams.matchId);
    }
}
