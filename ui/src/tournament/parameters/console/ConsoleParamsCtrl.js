import BaseTrParamsCtrl from 'tournament/parameters/BaseTrParamsCtrl.js';

export default class ConsoleParamsCtrl extends BaseTrParamsCtrl {
    static get $inject() {
        return ['auth', 'requestStatus', '$http'].concat(super.$inject);
    }

    createConsoleTournament(tid) {
        this.requestStatus.startLoading();
        this.$http.post('/api/tournament/console/create', tid,
                        {headers: {'Content-Type': 'application/json',
                                   session: this.auth.mySession()}}).
            then(
                (ok) => {
                    this.tournament.consoleTid = ok.data;
                    if (this.tournament.rules.group) {
                        this.tournament.rules.group.console = 'INDEPENDENT_RULES';
                    }
                    this.requestStatus.complete();
                },
                this.requestStatus.failed);
    }

    watchForPlayConsoleTournament() {
        this.$scope.$watch('$ctrl.playConsoleTournament', (newv, old) => {
            if (!this.tournament) {
                return;
            }
            if (newv && !this.tournament.consoleTid) {
                this.createConsoleTournament(this.tournament.tid);
            } else if (!newv && this.tournament.rules.group) {
                this.tournament.rules.group.console = 'NO';
            }
        });
    }

    onTournamentSet() {
        super.onTournamentSet(...arguments);
        this.playConsoleTournament = this.tournament.rules.group &&
            this.rules.group.console != 'NO';
    }

    constructor() {
        super(...arguments);
        this.playConsoleTournament = false;
        this.watchForPlayConsoleTournament();
    }
}
