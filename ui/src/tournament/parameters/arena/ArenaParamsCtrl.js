import BaseTrParamsCtrl from 'tournament/parameters/BaseTrParamsCtrl.js';

export default class ArenaParamsCtrl extends BaseTrParamsCtrl {
    get readyEvent() {
        return 'event.tournament.rules.arena.ready';
    }
}
