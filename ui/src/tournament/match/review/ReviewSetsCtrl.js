import SimpleController from 'core/angular/SimpleController.js';

export default class ReviewSetsCtrl extends SimpleController {
    static get $inject () {
        return ['$routeParams', 'binder', '$rootScope', '$scope'];
    }

    $onInit() {
        this.strong = {};
        this.config = {edit: false};
        this.tournamentId = this.$routeParams.tournamentId;

        this.binder(this.$scope, {
            'event.review.match.strong.data': (event, strong) => {
                this.strong = strong;
            },
            'event.review.match.config': (event, config) => {
                this.config = config;
            },
            'event.review.match.data': (event, match) => {
                this.matchReview = match;
                this.matchScore = this.matchReview.score;
                this.participants = this.matchReview.participants;
                let result = [];
                if (this.participants && this.participants.length == 2 && this.matchScore.sets) {
                    let l = this.matchScore.sets[this.participants[0].bid].length;
                    for (let i of l) {
                        result.push({a: this.matchScore.sets[this.participants[0].bid][i],
                                     b: this.matchScore.sets[this.participants[1].bid][i]});
                    }
                }
                this.sets = result;
            }
        });
        this.$rootScope.$broadcast('event.review.match.ready');
    }

    strongSet(iSet) {
        return this.strong[iSet];
    }

    appendSet() {
        this.$rootScope.$broadcast('event.review.match.set.appended');
    }

    removeLastSet() {
        this.$rootScope.$broadcast('event.review.match.set.popped');
    }

    pickSet(setIdx, set) {
        this.$rootScope.$broadcast('event.review.match.set.picked', setIdx, set);
    }
}
