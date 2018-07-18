import BaseTrParamsCtrl from 'tournament/parameters/BaseTrParamsCtrl.js';
import backedUpValue from 'core/backedUpValue.js';

function defaultPlayOffRules() {
   return {tpm: 0, losings: 1};
}

export default class PlayOffParamsCtrl extends BaseTrParamsCtrl {
    static get $inject() {
        return ['$timeout'].concat(super.$inject);
    }

    watchForPlayOff() {
        this.$scope.$watch('$ctrl.usePlayOff', (newValue) => {
            console.log(`usePlayOff change ${newValue}`);
            this.rules.playOff = this.playOffBackup.map(newValue);
        });
    }

    watchForConsoleLayered() {
        this.$scope.$watch('$ctrl.rules.casting.splitPolicy', (nv, ov) => {
            if (!this.rules.playOff) {
                return;
            }
            if (nv == 'cl' && ov != 'cl') {
                this.rules.playOff.lrCon = 'ml';
            } else if (nv && nv != 'cl' && ov == 'cl') {
                delete this.rules.playOff.lrCon;
            }
        });
    }

    onTournamentSet(tournament) {
        super.onTournamentSet(tournament);
        this.watchForPlayOff();
        this.watchForConsoleLayered();
        this.usePlayOff = !!this.rules.playOff;
    }

    constructor() {
        super(...arguments);
        this.playOffBackup = backedUpValue(
            defaultPlayOffRules, () => this.rules.playOff);
        this.usePlayOff = false;
        this.scrollBottom = () => this.$timeout(
            () => window.scrollTo(0, document.body.scrollHeight), 100);
    }
}
