import BaseTrParamsCtrl from 'tournament/parameters/BaseTrParamsCtrl.js';
import backedUpValue from 'core/backedUpValue.js';

function defaultProvidedRankOptions() {
    return {maxValue: 10000, minValue: 0, label: 'rating'};
}

export default class SeedingTournamentParamsCtrl extends BaseTrParamsCtrl {
    watchForPolicy() {
        this.$scope.$watch('$ctrl.rules.casting.policy', (rankPolicy) => {
            console.log(`rules.casting.policy: ${rankPolicy}`);
            this.rules.casting.pro = this.rankOptionsBackup.map(rankPolicy);
        });
    }

    onTournamentSet() {
        super.onTournamentSet(...arguments);
        this.rankingPolicyDomain = ['pr', 'm', 'su'];
        if (this.tournament.masterTid) {
            this.rankingPolicyDomain.push('mo');
        }
        this.watchForPolicy();
    }

    constructor() {
        super(...arguments);
        this.rank = {min: 0, max: 1000000};
        this.rankOptionsBackup = backedUpValue(defaultProvidedRankOptions,
                                               () => this.rules.casting.pro,
                                               'pr');
    }
}
