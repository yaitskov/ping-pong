import { setupAngularJs, ij } from 'test/angularjs-test-setup.js';

describe('console-tr-params', () => {
    var initEventFired = false;

    const ctx = setupAngularJs('console-tr-params', (scope) => {
        scope.$on('event.tournament.rules.console.ready', ($event) => {
            initEventFired = true;
        });
    });

    it('ready event is emitted', () => {
        expect(initEventFired).toBeTrue();
    });

    it('component is hidden initially', () => {
        expect(ctx.element.find('#console-tournament-toggler').hasClass('ng-hide')).toBeTrue();
        expect(ctx.element.find('#console-tournament-parameters').hasClass('ng-hide')).toBeTrue();
    });

    const newTournament = () => { return {rules: {group: {console: 'NO'}}}; };

    ij('component is not visible if new tournament', ($rootScope) => {
        $rootScope.$broadcast('event.tournament.rules.set', newTournament());
        ctx.sync();

        expect(ctx.element.find('#console-tournament-toggler').hasClass('ng-hide')).toBeTrue();
        expect(ctx.element.find('#console-tournament-parameters').hasClass('ng-hide')).toBeTrue();
    });

    const tournamentWithoutConsole = () => { return {tid: 1, rules: {group: {console: 'NO'}}}; };

    ij('component is visible if tournament has groups', ($rootScope) => {
        $rootScope.$broadcast('event.tournament.rules.set', tournamentWithoutConsole());
        ctx.sync();

        expect(ctx.ctrl.playConsoleTournament).toBeFalse();
        expect(ctx.element.find('#console-tournament-toggler').hasClass('ng-hide')).toBeFalse();
        expect(ctx.element.find('#console-tournament-parameters').hasClass('ng-hide')).toBeTrue();
    });

    const tournamentRequiresConsole = () => { return {tid: 1, rules: {group: {console: 'INDEPENDENT_RULES'}}}; };

    ij('console rules link is visible', ($rootScope) => {
        const tournamentWithConsole = Object.assign({consoleTid: 2}, tournamentRequiresConsole());
        $rootScope.$broadcast('event.tournament.rules.set', tournamentWithConsole);
        ctx.sync();

        expect(ctx.ctrl.playConsoleTournament).toBeTrue();
        expect(ctx.element.find('#console-tournament-toggler').hasClass('ng-hide')).toBeFalse();
        expect(ctx.element.find('#console-tournament-parameters').hasClass('ng-hide')).toBeFalse();
        expect(ctx.element.find('#console-tournament-parameters .btn-primary').hasClass('ng-hide')).toBeFalse();
    });

    ij('console tournament is created consoleTid is missing', ($rootScope, jsHttpBackend) => {
        const tour = tournamentRequiresConsole();
        const consoleTid = 2;

        $rootScope.$broadcast('event.tournament.rules.set', tour);

        jsHttpBackend.onPostMatch(/api.tournament.console.create/, [e => e.toBe(tour.tid)]).
            respondObject(consoleTid);

        ctx.sync(); // watcher

        expect(ctx.element.find('#console-tournament-parameters .btn-primary').
               hasClass('ng-hide')).toBeTrue();
        expect(ctx.element.find('#console-tournament-parameters .btn-default').
               hasClass('ng-hide')).toBeFalse();

        jsHttpBackend.flush(); // response callback

        expect(tour.rules.group.console).toBe('INDEPENDENT_RULES');
        expect(tour.consoleTid).toEqual(consoleTid);
        expect(ctx.element.find('#console-tournament-parameters .btn-primary').
               attr('href')).toBe(`#!/my/tournament/parameters/${consoleTid}`);
        expect(ctx.element.find('#console-tournament-parameters .btn-primary').
               hasClass('ng-hide')).toBeFalse();
        expect(ctx.element.find('#console-tournament-parameters .btn-default').
               hasClass('ng-hide')).toBeTrue();
    });

    ij('console tournament is created by toggle', ($rootScope, jsHttpBackend) => {
        const tour = tournamentWithoutConsole();
        $rootScope.$broadcast('event.tournament.rules.set', tour);
        ctx.sync(); // watcher
        jsHttpBackend.onPostMatch(/api.tournament.console.create/, [e => e.toBe(tour.tid)]).
            respondObject(2/*consoleTid*/);
        ctx.element.find('#console-tournament-toggler input').bootstrapToggle('on');
        jsHttpBackend.flush(); // response callback
        expect(tour.rules.group.console).toBe('INDEPENDENT_RULES');
    });
});
