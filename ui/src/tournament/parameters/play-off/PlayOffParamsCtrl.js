import BaseTrParamsCtrl from 'tournament/parameters/BaseTrParamsCtrl.js';
import backedUpValue from 'core/backedUpValue.js';
import HeadLessPlayOffMatchParamsCtrl from './HeadLessPlayOffMatchParamsCtrl.js';

function defaultPlayOffRules() {
   return {tpm: 0, losings: 1};
}

export default class PlayOffParamsCtrl extends BaseTrParamsCtrl {
    static get $inject() {
        return ['MessageBus', '$timeout'].concat(super.$inject);
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

    watchForMatchRulesInPlayOff() {
        this.$scope.$watch('$ctrl.useCustomPlayOffMatchRules', (newValue) => {
            console.log(`useCustomPlayOffMatchRules change ${newValue}`);
            if (this.rules.playOff) {
                this.rules.playOff.match = this.matchRulesInPlayOffBackup.map(newValue);
                if (this.rules.playOff.match) {
                    this.setCustomMatchRules();
                }
            }
        });
    }

    onTournamentSet(tournament) {
        super.onTournamentSet(tournament);
        this.watchForPlayOff();
        this.watchForConsoleLayered();
        this.watchForMatchRulesInPlayOff();
        this.usePlayOff = !!this.rules.playOff;
        if (this.usePlayOff) {
            this.useCustomPlayOffMatchRules = !!this.rules.playOff.match;
        }
        this.setCustomMatchRules();
    }

    setCustomMatchRules() {
        this.MessageBus.broadcast(
            HeadLessPlayOffMatchParamsCtrl.TopicLoad, this.rules.playOff);
    }

    constructor() {
        super(...arguments);
        this.playOffBackup = backedUpValue(
            defaultPlayOffRules,
            () => this.rules.playOff);
        this.matchRulesInPlayOffBackup = backedUpValue(
            () => Object.assign({}, this.rules.match),
            () => (this.rules.playOff || {}).match);
        this.usePlayOff = false;
        this.scrollBottom = () => this.$timeout(
            () => window.scrollTo(0, document.body.scrollHeight), 100);
    }
}
