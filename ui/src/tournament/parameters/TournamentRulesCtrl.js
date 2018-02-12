import CompositeCtrl from 'core/angular/CompositeCtrl.js';

import SeedingTournamentParamsCtrl from './seeding/SeedingTournamentParamsCtrl.js';
import PlayOffParamsCtrl from './play-off/PlayOffParamsCtrl.js';
import MatchParamsCtrl from './match/MatchParamsCtrl.js';
import GroupParamsCtrl from './group/GroupParamsCtrl.js';
import ArenaParamsCtrl from './arena/ArenaParamsCtrl.js';
import ConsoleParamsCtrl from './console/ConsoleParamsCtrl.js';

export default class TournamentRulesCtrl extends CompositeCtrl {
    update() {
        this.form.$setSubmitted();
        if (this.form.$valid && this.isValid) {
            this.broadcast('event.tournament.rules.update', this.tournament.rules);
        }
    }

    static get readyEvent() {
        return 'event.tournament.rules.ready';
    }

    get expectedChildCtrls() {
        return [SeedingTournamentParamsCtrl, PlayOffParamsCtrl,
                MatchParamsCtrl, GroupParamsCtrl, ArenaParamsCtrl,
                ConsoleParamsCtrl];
    }

    constructor() {
        super(...arguments);
        this.$bind({
            'event.tournament.rules.errors': (e, errors) => this.errors = errors,
            'event.tournament.rules.set': (e, tournament) => this.tournament = tournament
        });
    }

    back() {
        this.broadcast('event.tournament.rules.back', this.rules);
    }

    cancel() {
        this.broadcast('event.tournament.rules.cancel', this.rules);
    }
}
