import { setupAngularJs } from 'test/angularjs-test-setup.js';
import { checkTouchSpinIncrease, checkTouchSpinDecrease } from 'test/touchSpin.js';
import SeedingTournamentParamsCtrl from './SeedingTournamentParamsCtrl.js';
import defaultTournamentRules from 'tournament/new/defaultTournamentRules.js';

describe('seeding-tr-params', () => {
    var initEventFired = false;

    const ctx = setupAngularJs(
        'seeding-tr-params',
        {onInit: s => s.$on(SeedingTournamentParamsCtrl.readyEvent, e => initEventFired = true),
         parentCtrl: 'tournament-parameters-form'});

    it('ready event is emitted', () => {
        expect(initEventFired).toBeTrue();
    });

    const tournamentWithPlayOff = () => { return {rules: defaultTournamentRules('PingPong')}; };

    it('ranking policy toggles', () => {
        ctx.broadcast('event.tournament.rules.set', tournamentWithPlayOff());
        ctx.btnArrayToggles('#ranking-policy a',
                            () => ctx.ctrl.rules.casting.policy,
                            ['ProvidedRating', 'Manual', 'SignUp']);
    });
});
