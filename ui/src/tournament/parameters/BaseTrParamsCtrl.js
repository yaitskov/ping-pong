import ComposableCtrl from 'core/angular/ComposableCtrl.js';

export default class BaseTrParamsCtrl extends ComposableCtrl {
    static get $inject() {
        return ['$scope', 'binder'].concat(super.$inject);
    }

    onTournamentSet(tournament) {
        this.tournament = tournament;
        this.rules = tournament.rules;
    }

    onInitChild() {
        this.binder(this.$scope, {
            'event.tournament.rules.errors': (e, errors) => this.errors = errors,
            'event.tournament.rules.set': (e, t) => this.onTournamentSet(t)
        });
    }
}
