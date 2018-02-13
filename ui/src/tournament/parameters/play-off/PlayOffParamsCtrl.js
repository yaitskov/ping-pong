import BaseTrParamsCtrl from 'tournament/parameters/BaseTrParamsCtrl.js';

function defaultPlayOffRules() {
   return {thirdPlaceMatch: 0, losings: 1};
}

export default class PlayOffParamsCtrl extends BaseTrParamsCtrl {
    static get $inject() {
        return ['$timeout'].concat(super.$inject);
    }

    scrollBottom() {
        this.$timeout(() => window.scrollTo(0, document.body.scrollHeight), 100);
    }

    watchForPlayOff() {
        this.$scope.$watch('$ctrl.usePlayOff', (newValue, oldValue) => {
            console.log(`usePlayOff change ${newValue} ${oldValue}`);
            if (!this.rules) {
                return;
            }
            if (newValue) {
                if (this.rules.playOff) {
                    return;
                }
                this.rules.playOff = (this.playOffBackup
                    ? this.playOffBackup
                    : defaultPlayOffRules());
            } else {
                this.playOffBackup = this.rules.playOff;
                delete this.rules.playOff;
            }
        });
    }

    onTournamentSet(tournament) {
        super.onTournamentSet(tournament);
        this.usePlayOff = !!this.rules.playOff;
    }

    constructor() {
        super(...arguments);
        this.playOffBackup = null;
        this.usePlayOff = false;
        this.watchForPlayOff();
    }
}
