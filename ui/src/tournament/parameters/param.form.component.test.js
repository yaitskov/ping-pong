import { setupAngularJs, ij } from 'test/angularjs-test-setup.js';

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
            expect(spy).toHaveBeenCalledWith('event.tournament.rules.cancel', ctx.ctrl.rules);
        });
    });

    describe('back', () => {
        ij('emit event', ($rootScope) => {
            const spy = spyOn($rootScope, '$broadcast');
            ctx.ctrl.rules = {};
            ctx.element.find('#back-to-tournament-properties').click();
            expect(spy).toHaveBeenCalledWith('event.tournament.rules.back', ctx.ctrl.rules);
        });
    });

    describe('new tournament', () => {
        it('update button is not visible', () => {
            expect(ctx.element.find('#update-tournament-rules').hasClass('ng-hide')).toBeTrue();
        });

        it('create button is visible', () => {
            expect(ctx.element.find('#create-tournament').hasClass('ng-hide')).toBeFalse();
        });

        it('back button is visible', () => {
            expect(ctx.element.find('#back-to-tournament-properties').hasClass('ng-hide')).toBeFalse();
        });
    });

    describe('existing tournament', () => {
        ij('update button is not visible', ($rootScope) => {
            $rootScope.$broadcast('event.tournament.rules.set', {tid: 9, rules: {}});
            ctx.sync();
            expect(ctx.element.find('#update-tournament-rules').hasClass('ng-hide')).toBeFalse();
        });

        ij('create button is visible', ($rootScope) => {
            $rootScope.$broadcast('event.tournament.rules.set', {tid: 9, rules: {}});
            ctx.sync();
            expect(ctx.element.find('#create-tournament').hasClass('ng-hide')).toBeTrue();
        });

        ij('back button is visible', ($rootScope) => {
            $rootScope.$broadcast('event.tournament.rules.set', {tid: 9, rules: {}});
            ctx.sync();
            expect(ctx.element.find('#back-to-tournament-properties').hasClass('ng-hide')).toBeTrue();
        });
    });

    ij('set event', ($rootScope) => {
        $rootScope.$broadcast('event.tournament.rules.set', {tid: 9, rules: {}});
        expect(ctx.ctrl.tournamentId).toBe(9);
    });
});
