import injectArgs from 'angular-di.js';

export default class MatchParamsCtrl {
    static get $inject() {
        return ['$scope', '$rootScope', 'binder'];
    }

    onTournamentSet(tournament) {
        this.tournament = tournament;
        this.rules = tournament.rules;
    }

    constructor() {
        injectArgs(this, arguments);
        this.binder(this.$scope, {
            'event.tournament.rules.set': (e, t) => this.onTournamentSet(t)
        });
        this.$rootScope.$broadcast('event.tournament.rules.match.ready');
    }
}
