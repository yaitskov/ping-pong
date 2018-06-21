import { setupAngularJs, ij } from 'test/angularjs-test-setup.js';
import { existingTournament, newTournament } from 'test/defaultTournaments.js';

describe('tournament-parameters-form', () => {
    var initEventFired = false;
    const sport = 'PP';
    const ctx = setupAngularJs(
        'tournament-parameters-form',
        {onInit: (scope) => {
            scope.$on('event.tournament.rules.ready', ($event) => {
                initEventFired = true;
            });
        }});

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
        it('update button is not visible', () => ctx.hidden('#update-tournament-rules'));
        it('create button is visible', () => ctx.visible('#create-tournament'));
        it('back button is visible', () => ctx.visible('#back-to-tournament-properties'));
    });

    describe('existing tournament', () => {
        it('update button is not visible', () => {
            ctx.broadcast('event.tournament.rules.set', existingTournament(sport));
            ctx.visible('#update-tournament-rules');
        });
        it('create button is visible', () => {
            ctx.broadcast('event.tournament.rules.set', existingTournament(sport));
            ctx.hidden('#create-tournament');
        });
        it('back button is visible', () => {
            ctx.broadcast('event.tournament.rules.set', existingTournament(sport));
            ctx.hidden('#back-to-tournament-properties');
        });
    });

    it('set event', () => {
        const tournament = existingTournament('PP');
        ctx.broadcast('event.tournament.rules.set', tournament);
        expect(ctx.ctrl.tournament.tid).toBe(tournament.tid);
    });

    ij('default new tr rules pass', ($rootScope) => {
        const tournament = newTournament('PP');
        ctx.broadcast('event.tournament.rules.set', tournament);

        const spy = spyOn($rootScope, '$broadcast');
        ctx.element.find('#create-tournament').click();
        expect(spy).toHaveBeenCalledWith('event.tournament.rules.update',
                                         tournament.rules);
    });

    ij('default update tr rules pass', ($rootScope) => {
        const tournament = existingTournament('PP');
        ctx.broadcast('event.tournament.rules.set', tournament);
        const spy = spyOn($rootScope, '$broadcast');
        ctx.element.find('#update-tournament-rules').click();
        expect(spy).toHaveBeenCalledWith('event.tournament.rules.update',
                                         tournament.rules);
    });

    ij('child component validation works', ($rootScope) => {
        const tournament = newTournament('PP');
        const veryLongLabel = ''.padEnd(111, 'x');
        tournament.rules.casting.pro.label = veryLongLabel; // to long
        ctx.broadcast('event.tournament.rules.set', tournament);

        const spy = spyOn($rootScope, '$broadcast');
        ctx.element.find('#create-tournament').click();
        expect(spy).not.toHaveBeenCalledWith('event.tournament.rules.update', jasmine.any(Object));
    });
});
