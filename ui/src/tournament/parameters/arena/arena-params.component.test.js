import { setupAngularJs, ij } from 'test/angularjs-test-setup.js';
import ArenaParamsCtrl from './ArenaParamsCtrl.js';
import defaultTournamentRules from 'tournament/new/defaultTournamentRules.js';

describe('arena-params', () => {
    var initEventFired = false;

    const ctx = setupAngularJs(
        'arena-params',
        {onInit: s => s.$on(ArenaParamsCtrl.readyEvent, e => initEventFired = true),
         parentCtrl: 'tournament-parameters-form'});

    it('ready event is emitted', () => {
        expect(initEventFired).toBeTrue();
    });

    const tennisTournament = () => { return {rules: defaultTournamentRules('Tennis')}; };
    const pingPongTournament = () => { return {rules: defaultTournamentRules('PingPong')}; };

    ij('disambiguate strategy toggles', ($rootScope) => {
        $rootScope.$broadcast('event.tournament.rules.set', tennisTournament());
        ctx.sync();
        expect(ctx.ctrl.rules.place.arenaDistribution).toBe('NO');
        ctx.element.find('#arena-parameters .btn-danger').click();
        ctx.sync();
        expect(ctx.ctrl.rules.place.arenaDistribution).toBe('NO');
        ctx.element.find('#arena-parameters a:not(.btn-danger)').click();
        ctx.sync();
        expect(ctx.ctrl.rules.place.arenaDistribution).toBe('GLOBAL');
        ctx.element.find('#arena-parameters a:not(.btn-primary)').click();
        ctx.sync();
        expect(ctx.ctrl.rules.place.arenaDistribution).toBe('NO');
    });

    ij('tennis arena label is only visible', ($rootScope) => {
        $rootScope.$broadcast('event.tournament.rules.set', tennisTournament());
        ctx.sync();
        expect(ctx.element.find('.toggle-btn label.ng-hide').text()).toBe('Tables');
        expect(ctx.element.find('.toggle-btn label:not(.ng-hide)').text()).toBe('Courts');
    });

    ij('ping-pong arena label is only visible', ($rootScope) => {
        $rootScope.$broadcast('event.tournament.rules.set', pingPongTournament());
        ctx.sync();
        expect(ctx.element.find('.toggle-btn label.ng-hide').text()).toBe('Courts');
        expect(ctx.element.find('.toggle-btn label:not(.ng-hide)').text()).toBe('Tables');
    });
});
