import BaseTrParamsCtrl from 'tournament/parameters/BaseTrParamsCtrl.js';
import backedUpValue from 'core/backedUpValue.js';

function defaultProvidedRankOptions() {
    return {maxValue: 10000, minValue: 0, label: 'rating'};
}

export default class SeedingTournamentParamsCtrl extends BaseTrParamsCtrl {
    watchForPolicy() {
        this.$scope.$watch('$ctrl.rules.casting.policy', (rankPolicy) => {
            console.log(`rules.casting.policy: ${rankPolicy}`);
            this.rules.casting.providedRankOptions = this.rankOptionsBackup.map(rankPolicy);
        });
    }

    onTournamentSet() {
        super.onTournamentSet(...arguments);
        this.watchForPolicy();
    }

    constructor() {
        super(...arguments);
        this.rank = {min: 0, max: 1000000};
        this.rankOptionsBackup = backedUpValue(defaultProvidedRankOptions,
                                               () => this.rules.casting.providedRankOptions,
                                               'ProvidedRating');
    }
}
