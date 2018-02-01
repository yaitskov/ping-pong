import { setupAngularJs, ij } from 'test/angularjs-test-setup.js';

describe('confirm-participant-expel', () => {
    var initEventFired = false;

    const ctx = setupAngularJs('confirm-participant-expel', (scope) => {
        scope.$on('event.confirm-participant-expel.ready', ($event) => {
            initEventFired = true;
        });
    });

    it('ready event is emitted', () => {
        expect(initEventFired).toBeTrue();
    });

    const waitingBid = {user: {name: 'p1', uid: 2}, state: 'Wait', tid: 1};

    ij('confirm event shows the dialog', ($rootScope) => {
        expect(ctx.element.find('#confirmParticipantExpel').hasClass('in')).toBeFalse();

        const bid = Object.assign({}, waitingBid);

        $rootScope.$broadcast('event.confirm-participant-expel.confirm', bid);
        ctx.sync();

        expect(ctx.ctrl.bid).toBe(bid);
        expect(ctx.element.find('#confirmParticipantExpel').hasClass('in')).toBeTrue();
    });

    ij('expelAs quit and hide dialog', (jsHttpBackend, $rootScope) => {
        const bid = Object.assign({}, waitingBid);

        $rootScope.$broadcast('event.confirm-participant-expel.confirm', bid);

        jsHttpBackend.onPostMatch(/api.tournament.expel/,
                                          [e => e.toEqual(jasmine.objectContaining(
                                            {uid: 2, tid: 1, targetBidState: 'Quit'}))]).
                    respondObject('ok');

        ctx.element.find('#expel-as-quit').click();

        jsHttpBackend.flush();
        expect(bid.state).toBe('Quit');
        expect(ctx.element.find('#confirmParticipantExpel').hasClass('in')).toBeFalse();
    });
});
