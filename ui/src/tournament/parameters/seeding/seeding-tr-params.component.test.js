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

    it('rank name is visible for policy provided rank', () => {
        ctx.broadcast('event.tournament.rules.set', tournamentWithPlayOff());
        ctx.visible('#rank-name-field');
        expect(ctx.element.find('#rankName').val()).
            toBe(ctx.ctrl.rules.casting.providedRankOptions.label);
    });

    const tournamentWithManualRanking = () => {
        const tournament = tournamentWithPlayOff();
        delete tournament.rules.casting.providedRankOptions;
        tournament.rules.casting.policy = 'Manual';
        return tournament;
    };

    it('rank name is not visible for policy other than provided rank', () => {
        ctx.broadcast('event.tournament.rules.set', tournamentWithManualRanking());
        ctx.hidden('#rank-name-field');
    });

    it('rank name length validation', () => {
        ctx.broadcast('event.tournament.rules.set', tournamentWithPlayOff());
        ctx.setValue('#rankName', ''.padEnd(101, 'x'));
        ctx.visible('#rank-name-field .help-block');
        ctx.hasClass('#rank-name-field', 'has-error');

        ctx.setValue('#rankName', 'xxx');
        ctx.hidden('#rank-name-field .help-block');
        ctx.hasNoClass('#rank-name-field', 'has-error');
    });
});
