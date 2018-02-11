import BaseTrParamsCtrl from 'tournament/parameters/BaseTrParamsCtrl.js';

function defaultProvidedRankOptions() {
    return {maxValue: 10000, minValue: 0, label: 'rating'};
}

export default class SeedingTournamentParamsCtrl extends BaseTrParamsCtrl {
    watchForPolicy() {
        this.$scope.$watch('$ctrl.rules.casting.policy', (newValue, oldValue) => {
            console.log(`rules.casting.policy: new value ${newValue} old value ${oldValue}`);
            if (!newValue || !oldValue) {
                return;
            }
            if (newValue == 'ProvidedRating') {
                this.rules.casting.providedRankOptions = (
                   this.providedRankOptionsBackup
                      ? this.providedRankOptionsBackup
                      : defaultProvidedRankOptions());
            } else {
                this.providedRankOptionsBackup = this.rules.casting.providedRankOptions;
                delete this.rules.casting.providedRankOptions;
            }
        });
    }

    constructor() {
        super(...arguments);
        this.rank = {min: 0, max: 1000000};
        this.providedRankOptionsBackup = null;
        this.watchForPolicy();
    }
}
