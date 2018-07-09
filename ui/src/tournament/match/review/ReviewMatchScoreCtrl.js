import SimpleController from 'core/angular/SimpleController.js';

export default class ReviewMatchScoreCtrl extends SimpleController {
    static get $inject () {
        return ['mainMenu', 'pageCtx', '$routeParams', 'binder', '$rootScope', '$scope'];
    }

    $onInit() {
        this.tournamentId = this.$routeParams.tournamentId;
        this.matchId = this.$routeParams.matchId;
        this.mainMenu.setTitle('Match Review');

        this.binder(this.$scope, {
            'event.review.match.ready': (event) => {
                this.$rootScope.$broadcast(
                    'event.review.match.data',
                    this.pageCtx.get('match-score-review-' + this.$routeParams.matchId));
            }
        });
    }
}
