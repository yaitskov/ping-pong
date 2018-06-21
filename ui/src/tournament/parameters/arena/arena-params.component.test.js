import { setupAngularJs } from 'test/angularjs-test-setup.js';
import ArenaParamsCtrl from './ArenaParamsCtrl.js';
import { newTournament } from 'test/defaultTournaments.js';

describe('arena-params', () => {
    var initEventFired = false;

    const ctx = setupAngularJs(
        'arena-params',
        {onInit: s => s.$on(ArenaParamsCtrl.readyEvent, e => initEventFired = true),
         parentCtrl: 'tournament-parameters-form'});

    it('ready event is emitted', () => {
        expect(initEventFired).toBeTrue();
    });

    const tennisTournament = () => newTournament('Tennis');
    const pingPongTournament = () => newTournament('PingPong');

    it('disambiguate strategy toggles', () => {
        ctx.broadcast('event.tournament.rules.set', tennisTournament());
        expect(ctx.ctrl.rules.place.ar).toBe('NO');
        ctx.click('#arena-parameters .btn-danger');
        expect(ctx.ctrl.rules.place.ar).toBe('NO');
        ctx.click('#arena-parameters a:not(.btn-danger)');
        expect(ctx.ctrl.rules.place.ar).toBe('g');
        ctx.click('#arena-parameters a:not(.btn-primary)');
        expect(ctx.ctrl.rules.place.ar).toBe('NO');
    });

    it('tennis arena label is only visible', () => {
        ctx.broadcast('event.tournament.rules.set', tennisTournament());
        expect(ctx.element.find('.toggle-btn label.ng-hide').text()).toBe('Tables');
        expect(ctx.element.find('.toggle-btn label:not(.ng-hide)').text()).toBe('Courts');
    });

    it('ping-pong arena label is only visible', () => {
        ctx.broadcast('event.tournament.rules.set', pingPongTournament());
        expect(ctx.element.find('.toggle-btn label.ng-hide').text()).toBe('Courts');
        expect(ctx.element.find('.toggle-btn label:not(.ng-hide)').text()).toBe('Tables');
    });
});
