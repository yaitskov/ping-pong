import angular from 'angular';
import { setupAngularJs, templateRequiredFromTest } from 'test/angularjs-test-setup.js';
import { checkTouchSpinID, checkTouchSpinNotIncrease } from 'test/touchSpin.js';
import HeadLessMatchParamsCtrl from './HeadLessMatchParamsCtrl.js';
import { newTournament } from 'test/defaultTournaments.js';
import template from './headless-match-params.template.html';

angular.
    module('tournament').
    component('headLessMatchParams', {
        templateUrl: templateRequiredFromTest(template),
        controller: HeadLessMatchParamsCtrl});

describe('head-less-match-params', () => {
    const ctx = setupAngularJs('head-less-match-params');
    it('min game advance not increase limit', () => {
        const tournament = newTournament('PP');
        tournament.rules.match.maig = 1000;
        ctx.send(HeadLessMatchParamsCtrl.TopicLoad, tournament.rules);
        checkTouchSpinNotIncrease(ctx, '#min-game-advance',
                                  () => ctx.ctrl.rules.match.maig);
    });

    it('min game advance changes', () => {
        ctx.send(HeadLessMatchParamsCtrl.TopicLoad,
                 newTournament('PP').rules);
        checkTouchSpinID(ctx, '#min-game-advance',
                         () => ctx.ctrl.rules.match.maig);
    });

    describe('count only sets', () => {
        beforeEach(() => {
            const tournamentRules = newTournament('PP').rules;
            tournamentRules.match.cos = true;
            ctx.send(HeadLessMatchParamsCtrl.TopicLoad, tournamentRules);
        });

        it('sets input visible', () => ctx.visible('#sets-to-win-match'));
        it('min-games-to-win hidden', () => ctx.hidden('#min-games-to-win'));
        it('min-game-advance hidden', () => ctx.hidden('#min-game-advance'));
        it('visible for ping pong', () => ctx.visible('#count-only-sets'));
    });

    it('count only sets hidden for tennis', () => {
        ctx.send(HeadLessMatchParamsCtrl.TopicLoad, newTournament('TE').rules);
        ctx.hidden('#count-only-sets');
    });
});
