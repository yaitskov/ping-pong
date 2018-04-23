import BaseTrParamsCtrl from 'tournament/parameters/BaseTrParamsCtrl.js';
import HeadLessCommonMatchParamsCtrl from './HeadLessCommonMatchParamsCtrl.js';

export default class MatchParamsCtrl extends BaseTrParamsCtrl {
    static get $inject() {
        return ['MessageBus'].concat(super.$inject);
    }

    constructor() {
        super(...arguments);
    }

    onTournamentSet(tournament) {
        super.onTournamentSet(tournament);
        this.MessageBus.broadcast(
            HeadLessCommonMatchParamsCtrl.TopicLoad, tournament.rules);
    }
}
