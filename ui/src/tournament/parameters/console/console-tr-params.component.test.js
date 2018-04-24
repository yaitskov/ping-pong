import { setupAngularJs, ij } from 'test/angularjs-test-setup.js';
import ConsoleParamsCtrl from './ConsoleParamsCtrl.js';
import { newTournament, existingTournamentWithoutGroup, existingTournamentWithoutConsole,
         existingTournamentWithConsole, existingTournamentRequiresConsole,
         existingTournamentWithGroup } from 'test/defaultTournaments.js';

describe('console-tr-params', () => {
    var initEventFired = false;
    const sport = 'PingPong';
    const ctx = setupAngularJs(
        'console-tr-params',
        {onInit: s => s.$on(ConsoleParamsCtrl.readyEvent, e => initEventFired = true),
         parentCtrl: 'tournament-parameters-form'});

    it('ready event is emitted', () => {
        expect(initEventFired).toBeTrue();
    });

    it('console panel is hidden initially', () => {
        ctx.hidden('#console-tournament-toggler');
        ctx.hidden('#console-tournament-parameters');
    });

    it('console panel is not visible if new tournament', () => {
        ctx.broadcast('event.tournament.rules.set', newTournament(sport));
        ctx.hidden('#console-tournament-toggler');
        ctx.hidden('#console-tournament-parameters');
    });

    it('console panel is not visible if tournament has no group', () => {
        ctx.broadcast('event.tournament.rules.set', existingTournamentWithoutGroup(sport));
        ctx.hidden('#console-tournament-toggler');
        ctx.hidden('#console-tournament-parameters');
    });

    it('console panel disappears once group is disabled', () => {
        ctx.broadcast('event.tournament.rules.set', existingTournamentWithGroup(sport));
        delete ctx.ctrl.rules.group;
        ctx.sync();
        ctx.hidden('#console-tournament-toggler');
        ctx.hidden('#console-tournament-parameters');
    });

    it('console options are not visible if tournament has no console tournament', () => {
        ctx.broadcast('event.tournament.rules.set', existingTournamentWithoutConsole(sport));
        expect(ctx.ctrl.playConsoleTournament).toBeFalse();
        ctx.visible('#console-tournament-toggler');
        ctx.hidden('#console-tournament-parameters');
    });

    it('console rules link is visible', () => {
        ctx.broadcast('event.tournament.rules.set', existingTournamentWithConsole(sport));
        expect(ctx.ctrl.playConsoleTournament).toBeTrue();
        ctx.visible('#console-tournament-toggler');
        ctx.visible('#console-tournament-parameters');
        ctx.visible('#console-tournament-parameters .btn-primary');
    });

    ij('console tournament is created consoleTid is missing', (jsHttpBackend) => {
        const tour = existingTournamentRequiresConsole(sport);
        const consoleTid = 2;

        jsHttpBackend.onPostMatch(/api.tournament.console.create/, [e => e.toBe(tour.tid)]).
            respondObject(consoleTid);

        ctx.broadcast('event.tournament.rules.set', tour);

        ctx.hidden('#console-tournament-parameters .btn-primary');
        ctx.visible('#console-tournament-parameters .btn-default');

        jsHttpBackend.flush(); // response callback

        expect(tour.rules.group.console).toBe('INDEPENDENT_RULES');
        expect(tour.consoleTid).toEqual(consoleTid);
        expect(ctx.element.find('#console-tournament-parameters .btn-primary').
               attr('href')).toBe(`#!/my/tournament/parameters/${consoleTid}`);
        ctx.visible('#console-tournament-parameters .btn-primary');
        ctx.hidden('#console-tournament-parameters .btn-default');
    });

    ij('console tournament is created by toggle', (jsHttpBackend) => {
        const tour = existingTournamentWithoutConsole(sport);
        ctx.broadcast('event.tournament.rules.set', tour);

        jsHttpBackend.onPostMatch(/api.tournament.console.create/, [e => e.toBe(tour.tid)]).
            respondObject(2/*consoleTid*/);
        ctx.element.find('#console-tournament-toggler input').bootstrapToggle('on');
        jsHttpBackend.flush(); // response callback
        expect(tour.rules.group.console).toBe('INDEPENDENT_RULES');
    });
});
