import { setupAngularJs, ij } from 'test/angularjs-test-setup.js';

describe('manage-one-participant', () => {
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
});
