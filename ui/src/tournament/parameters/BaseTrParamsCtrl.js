import AngularBean from 'core/angular/AngularBean.js';

export default class BaseTrParamsCtrl extends AngularBean {
    static get $inject() {
        return ['$scope', '$rootScope', 'binder'];
    }

    onTournamentSet(tournament) {
        this.tournament = tournament;
        this.rules = tournament.rules;
    }

    get readyEvent() {
        throw new Error("implement me");
    }

    get isValid() {
        return true;
    }

    $onInit() {
        this.parent.registerSection(this);
        this.binder(this.$scope, {
            'event.tournament.rules.errors': (e, errors) => this.errors = errors,
            'event.tournament.rules.set': (e, t) => this.onTournamentSet(t)
        });
        this.$rootScope.$broadcast(this.readyEvent);
    }
}
