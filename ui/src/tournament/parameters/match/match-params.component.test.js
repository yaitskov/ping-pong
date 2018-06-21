import { setupAngularJs, ij } from 'test/angularjs-test-setup.js';
import { checkTouchSpinID, checkTouchSpinNotIncrease } from 'test/touchSpin.js';
import MatchParamsCtrl from './MatchParamsCtrl.js';
import { newTournament } from 'test/defaultTournaments.js';

describe('match-params', () => {
    var initEventFired = false;

    const ctx = setupAngularJs(
        'match-params',
        {onInit: s => s.$on(MatchParamsCtrl.readyEvent, e => initEventFired = true),
         parentCtrl: 'tournament-parameters-form'});

    it('ready event is emitted', () => {
        expect(initEventFired).toBeTrue();
    });

    const tennisTournament = () => newTournament('TE');
    const pingPongTournament = () => newTournament('PP');

    it('min game advance increase/decrease', () => {
        ctx.broadcast('event.tournament.rules.set', pingPongTournament());
        checkTouchSpinID(ctx, '#min-game-advance', () => ctx.ctrl.rules.match.maig);
    });

    it('min games to win increase/decrease', () => {
        ctx.broadcast('event.tournament.rules.set', pingPongTournament());
        checkTouchSpinID(ctx, '#min-games-to-win', () => ctx.ctrl.rules.match.mgtw);
    });

    it('min sets to win increase/decrease', () => {
        ctx.broadcast('event.tournament.rules.set', pingPongTournament());
        checkTouchSpinID(ctx, '#sets-to-win-match', () => ctx.ctrl.rules.match.stw);
    });

    it('super tie break games increase/decrease', () => {
        ctx.broadcast('event.tournament.rules.set', tennisTournament());
        checkTouchSpinID(ctx, '#supertie-break-games', () => ctx.ctrl.rules.match.stbg);
    });

    it('super tie break games is visible for tennis only', () => {
        ctx.broadcast('event.tournament.rules.set', tennisTournament());
        ctx.visible('#supertie-break-games');
        ctx.broadcast('event.tournament.rules.set', pingPongTournament());
        ctx.hidden('#supertie-break-games');
    });

    it('min sets to win is not visible if cos', () => {
        const tournament = pingPongTournament();
        ctx.broadcast('event.tournament.rules.set', tournament);
        ctx.visible('#sets-to-win-match');
        ctx.visible('#min-games-to-win');
        ctx.visible('#min-game-advance');
        tournament.rules.match.cos = true;
        ctx.broadcast('event.tournament.rules.set', tournament);
        ctx.visible('#sets-to-win-match');
        ctx.hidden('#min-games-to-win');
        ctx.hidden('#min-game-advance');
    });

    it('3rd place match toggles', () => {
        ctx.broadcast('event.tournament.rules.set', pingPongTournament());
        ctx.btnTogglesDiffClasses('#count-only-sets',
                                  () => ctx.ctrl.rules.match.cos,
                                  {default: {clazz: 'btn-primary', value: false},
                                   other: {clazz: 'btn-success', value: true}});
    });
});
