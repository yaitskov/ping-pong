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

            ctx.element.find('#discard-rules-changes').click();
            expect(spy).toHaveBeenCalledWith('event.tournament.rules.cancel', ctx.ctrl.rules);
        });
    });
});
