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

    watchForMatchRulesInPlayOff() {
        this.$scope.$watch('$ctrl.useCustomPlayOffMatchRules', (newValue) => {
            console.log(`useCustomPlayOffMatchRules change ${newValue}`);
            this.rules.playOff.match = this.matchRulesInPlayOffBackup.map(newValue);
        });
    }

    onTournamentSet(tournament) {
        super.onTournamentSet(tournament);
        this.watchForPlayOff();
        this.watchForMatchRulesInPlayOff();
        this.usePlayOff = !!this.rules.playOff;
        if (this.usePlayOff) {
            this.useCustomPlayOffMatchRules = !!this.rules.playOff.match;
        }
        this.MessageBus.broadcast(
            HeadLessPlayOffMatchParamsCtrl.TopicLoad, tournament.rules.playOff);
    }

    constructor() {
        super(...arguments);
        this.playOffBackup = backedUpValue(defaultPlayOffRules, () => this.rules.playOff);
        this.matchRulesInPlayOffBackup = backedUpValue(
            () => Object.assign({}, this.rules.match),
            () => this.rules.playOff.match);
        this.usePlayOff = false;
        this.scrollBottom = () => this.$timeout(() => window.scrollTo(0, document.body.scrollHeight), 100);
    }
}
