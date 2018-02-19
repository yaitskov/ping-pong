import { setupAngularJs } from 'test/angularjs-test-setup.js';
import { checkTouchSpinID } from 'test/touchSpin.js';
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
        ctx.visible('#max-rank-value');
        ctx.visible('#min-rank-value');
        ctx.visible('#rank-axis-direction');

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
        ctx.hidden('#max-rank-value');
        ctx.hidden('#min-rank-value');
        ctx.hidden('#rank-axis-direction');
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

    it('rank axis direction toggles', () => {
        ctx.broadcast('event.tournament.rules.set', tournamentWithPlayOff());
        ctx.btnTogglesDiffClasses('#rank-axis-direction',
                                  () => ctx.ctrl.rules.casting.direction,
                                  {default: {clazz: 'btn-primary', value: 'Decrease'},
                                   other: {clazz: 'btn-primary', value: 'Increase'}});
    });

    it('max rank increase/decrease', () => {
        ctx.broadcast('event.tournament.rules.set', tournamentWithPlayOff());
        checkTouchSpinID(ctx, '#max-rank-value',
                         () => ctx.ctrl.rules.casting.providedRankOptions.maxValue);
    });

    it('min rank increase/decrease', () => {
        ctx.broadcast('event.tournament.rules.set', tournamentWithPlayOff());
        checkTouchSpinID(ctx, '#min-rank-value',
                         () => ctx.ctrl.rules.casting.providedRankOptions.minValue);
    });
});