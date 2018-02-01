import { setupAngularJs, ij } from 'test/angularjs-test-setup.js';

describe('manage-one-participant', () => {
    describe('waiting participant', () => {
        const waitingBid = {user: {name: 'p1', uid: 2}, state: 'Wait', tid: 1};

        const ctx = setupAngularJs('manage-one-participant', (scope, jsHttpBackend) => {
            jsHttpBackend.onGet(/api.bid.state/).respondObject(Object.assign({}, waitingBid));
        });

        ij('expel emits confirm event', ($rootScope, jsHttpBackend) => {
            jsHttpBackend.flush();
            const broadCastSpy = spyOn($rootScope, '$broadcast');
            expect(ctx.ctrl.participant.user.name).toBe('p1');
            ctx.element.find('#expel-participant-btn').click();
            expect(broadCastSpy).toHaveBeenCalledWith('event.confirm-participant-expel.confirm',
                                                      ctx.ctrl.participant);
        });

        ij('expel button is visible for bid in a terminal state', (jsHttpBackend) => {
            jsHttpBackend.flush();
            expect(ctx.element.find('#expel-participant-btn').hasClass('ng-hide')).toBeFalse();
        });
    });

    describe('quit participant', () => {
        const quitBid = {user: {name: 'p1', uid: 2}, state: 'Quit', tid: 1};

        const ctx = setupAngularJs('manage-one-participant', (scope, jsHttpBackend) => {
            jsHttpBackend.onGet(/api.bid.state/).respondObject(Object.assign({}, quitBid));
        });

        ij('expel button is not visible for bid in a terminal state', (jsHttpBackend) => {
            jsHttpBackend.flush();
            expect(ctx.element.find('#expel-participant-btn').hasClass('ng-hide')).toBeTrue();
        });
    });
});
