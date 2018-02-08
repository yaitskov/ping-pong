import injectArgs from 'angular-di.js';

export default class SeedingTournamentParamsCtrl {
    static get $inject() {
        return ['$scope', '$rootScope', 'binder'];
    }

    onTournamentSet(tournament) {
        this.tournament = tournament;
    }

    constructor() {
        injectArgs(this, arguments);
        this.binder(this.$scope, {
            'event.tournament.rules.set': this.onTournamentSet
        });
        this.$rootScope.$broadcast('event.tournament.rules.seeding.ready');
    }
}
