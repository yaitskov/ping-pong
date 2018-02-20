import { setupAngularJs, ij } from 'test/angularjs-test-setup.js';
import PlayOffParamsCtrl from './PlayOffParamsCtrl.js';
import { newTournamentWithPlayOff, newTournamentWithoutPlayOff } from 'test/defaultTournaments.js';

describe('play-off-tr-params', () => {
    var initEventFired = false;

    const ctx = setupAngularJs(
        'play-off-tr-params',
        {onInit: s => s.$on(PlayOffParamsCtrl.readyEvent, e => initEventFired = true),
         parentCtrl: 'tournament-parameters-form'});

    it('ready event is emitted', () => {
        expect(initEventFired).toBeTrue();
    });

    it('play off panel is visible if tournament has play off', () => {
        ctx.broadcast('event.tournament.rules.set', newTournamentWithPlayOff());
        expect(ctx.ctrl.usePlayOff).toBeTrue();
        ctx.visible('#play-off-parameters');
        ctx.checked('#play-off-parameters-toggler input');
    });

    it('play off panel is hidden if tournament without play off', () => {
        ctx.broadcast('event.tournament.rules.set', newTournamentWithPlayOff());
        expect(ctx.ctrl.usePlayOff).toBeTrue();
        ctx.visible('#play-off-parameters');
        ctx.checked('#play-off-parameters-toggler input');
    });

    it('enable play off panel', () => {
        ctx.broadcast('event.tournament.rules.set', newTournamentWithoutPlayOff());
        ctx.toggleOn('#play-off-parameters-toggler input');
        expect(ctx.ctrl.rules.playOff.losings).toBe(1);
    });

    it('disable / enable play off panel', () => {
        ctx.broadcast('event.tournament.rules.set', newTournamentWithPlayOff());
        ctx.toggleOff('#play-off-parameters-toggler input');
        expect(ctx.ctrl.rules.playOff).toBeUndefined();
        ctx.toggleOn('#play-off-parameters-toggler input');
        expect(ctx.ctrl.rules.playOff.thirdPlaceMatch).toBe(1);
    });

    it('max losses in play off toggles', () => {
        ctx.broadcast('event.tournament.rules.set', newTournamentWithPlayOff());
        ctx.btnTogglesDiffClasses('#max-losses-in-play-off',
                                  () => ctx.ctrl.rules.playOff.losings,
                                  {default: {clazz: 'btn-primary', value: 1},
                                   other: {clazz: 'btn-success', value: 2}});
    });

    it('3rd place match toggles', () => {
        ctx.broadcast('event.tournament.rules.set', newTournamentWithPlayOff());
        ctx.btnTogglesDiffClasses('#third-place-match',
                                  () => ctx.ctrl.rules.playOff.thirdPlaceMatch,
                                  {default: {clazz: 'btn-primary', value: 1},
                                   other: {clazz: 'btn-danger', value: 0}});
    });
});
