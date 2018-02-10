import BaseTrParamsCtrl from 'tournament/parameters/BaseTrParamsCtrl.js';

export default class MatchParamsCtrl extends BaseTrParamsCtrl {
    get readyEvent() {
        return 'event.tournament.rules.match.ready';
    }

    constructor() {
        super(...arguments);
        this.advance = {min: 1, max: 1000};
        this.score = {min: 1, max: 1000};
        this.sets = {min: 1, max: 1000};
    }
}
