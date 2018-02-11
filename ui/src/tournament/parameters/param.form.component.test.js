import { setupAngularJs, ij } from 'test/angularjs-test-setup.js';
import defaultTournamentRules from 'tournament/new/defaultTournamentRules.js';

describe('tournament-parameters-form', () => {
    var initEventFired = false;
    const ctx = setupAngularJs('tournament-parameters-form', (scope) => {
        scope.$on('event.tournament.rules.ready', ($event) => {
            initEventFired = true;
        });
    });

    it('init event fired', () => expect(initEventFired).toBeTrue());

    describe('cancel', () => {
        ij('emit event', ($rootScope) => {
            const spy = spyOn($rootScope, '$broadcast');
            ctx.ctrl.rules = {};
            ctx.element.find('#discard-rules-changes').click();
            expect(spy).toHaveBeenCalledWith('event.tournament.rules.cancel',
                                             ctx.ctrl.rules);
        });
    });

    describe('back', () => {
        ij('emit event', ($rootScope) => {
            const spy = spyOn($rootScope, '$broadcast');
            ctx.ctrl.rules = {};
            ctx.element.find('#back-to-tournament-properties').click();
            expect(spy).toHaveBeenCalledWith('event.tournament.rules.back',
                                             ctx.ctrl.rules);
        });
    });

    describe('new tournament', () => {
        it('update button is not visible', () => {
            expect(ctx.element.find('#update-tournament-rules')
                   .hasClass('ng-hide')).toBeTrue();
        });

        it('create button is visible', () => {
            expect(ctx.element.find('#create-tournament')
                   .hasClass('ng-hide')).toBeFalse();
        });

        it('back button is visible', () => {
            expect(ctx.element.find('#back-to-tournament-properties')
                   .hasClass('ng-hide')).toBeFalse();
        });
    });

    describe('existing tournament', () => {
        ij('update button is not visible', ($rootScope) => {
            $rootScope.$broadcast('event.tournament.rules.set',
                                  {tid: 9, rules: {}});
            ctx.sync();
            expect(ctx.element.find('#update-tournament-rules')
                   .hasClass('ng-hide')).toBeFalse();
        });

        ij('create button is visible', ($rootScope) => {
            $rootScope.$broadcast('event.tournament.rules.set',
                                  {tid: 9, rules: {}});
            ctx.sync();
            expect(ctx.element.find('#create-tournament')
                   .hasClass('ng-hide')).toBeTrue();
        });

        ij('back button is visible', ($rootScope) => {
            $rootScope.$broadcast('event.tournament.rules.set',
                                  {tid: 9, rules: {}});
            ctx.sync();
            expect(ctx.element.find('#back-to-tournament-properties')
                   .hasClass('ng-hide')).toBeTrue();
        });
    });

    ij('set event', ($rootScope) => {
        $rootScope.$broadcast('event.tournament.rules.set',
                              {tid: 9, rules: defaultTournamentRules('PingPong')});
        expect(ctx.ctrl.tournament.tid).toBe(9);
    });

    ij('default new tr rules pass', ($rootScope) => {
        const tournament = {rules: defaultTournamentRules('PingPong')};
        $rootScope.$broadcast('event.tournament.rules.set', tournament);
        ctx.sync();

        const spy = spyOn($rootScope, '$broadcast');
        ctx.element.find('#create-tournament').click();
        expect(spy).toHaveBeenCalledWith('event.tournament.rules.update',
                                         tournament.rules);
    });

    ij('default update tr rules pass', ($rootScope) => {
        const tournament = {tid: 1, rules: defaultTournamentRules('PingPong')};
        $rootScope.$broadcast('event.tournament.rules.set', tournament);
        ctx.sync();

        const spy = spyOn($rootScope, '$broadcast');
        ctx.element.find('#update-tournament-rules').click();
        expect(spy).toHaveBeenCalledWith('event.tournament.rules.update',
                                         tournament.rules);
    });

    ij('child component validation works', ($rootScope) => {
        const tournament = {rules: defaultTournamentRules('PingPong')};
        const veryLongLabel = ''.padEnd(111, 'x');
        tournament.rules.casting.providedRankOptions.label = veryLongLabel; // to long
        $rootScope.$broadcast('event.tournament.rules.set', tournament);
        ctx.sync();

        const spy = spyOn($rootScope, '$broadcast');
        ctx.element.find('#create-tournament').click();
        expect(spy).not.toHaveBeenCalledWith('event.tournament.rules.update', jasmine.any(Object));
    });
});
