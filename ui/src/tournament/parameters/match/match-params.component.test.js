import { setupAngularJs, ij } from 'test/angularjs-test-setup.js';
import { checkTouchSpinID, checkTouchSpinNotIncrease } from 'test/touchSpin.js';
import MatchParamsCtrl from './MatchParamsCtrl.js';
import defaultTournamentRules from 'tournament/new/defaultTournamentRules.js';

describe('match-params', () => {
    var initEventFired = false;

    const ctx = setupAngularJs(
        'match-params',
        {onInit: s => s.$on(MatchParamsCtrl.readyEvent, e => initEventFired = true),
         parentCtrl: 'tournament-parameters-form'});

    it('ready event is emitted', () => {
        expect(initEventFired).toBeTrue();
    });

    const tennisTournament = () => { return {rules: defaultTournamentRules('Tennis')}; };
    const pingPongTournament = () => { return {rules: defaultTournamentRules('PingPong')}; };

    it('min game advance increase/decrease', () => {
        ctx.broadcast('event.tournament.rules.set', pingPongTournament());
        checkTouchSpinID(ctx, '#min-game-advance', () => ctx.ctrl.rules.match.minAdvanceInGames);
    });

    it('min games to win increase/decrease', () => {
        ctx.broadcast('event.tournament.rules.set', pingPongTournament());
        checkTouchSpinID(ctx, '#min-games-to-win', () => ctx.ctrl.rules.match.minGamesToWin);
    });

    it('min sets to win increase/decrease', () => {
        ctx.broadcast('event.tournament.rules.set', pingPongTournament());
        checkTouchSpinID(ctx, '#sets-to-win-match', () => ctx.ctrl.rules.match.setsToWin);
    });

    it('super tie break games increase/decrease', () => {
        ctx.broadcast('event.tournament.rules.set', tennisTournament());
        checkTouchSpinID(ctx, '#supertie-break-games', () => ctx.ctrl.rules.match.superTieBreakGames);
    });

    it('super tie break games is visible for tennis only', () => {
        ctx.broadcast('event.tournament.rules.set', tennisTournament());
        ctx.visible('#supertie-break-games');
        ctx.broadcast('event.tournament.rules.set', pingPongTournament());
        ctx.hidden('#supertie-break-games');
    });

    it('min game advance not increase limit', () => {
        expect(ctx.ctrl.advance.max).toBe(1000);
        ctx.broadcast('event.tournament.rules.set',
                      Object.assign(pingPongTournament(),
                                    {rules: {match: {minAdvanceInGames: 1000}}}));
        checkTouchSpinNotIncrease(ctx, '#min-game-advance',
                                  () => ctx.ctrl.rules.match.minAdvanceInGames);
    });
});
