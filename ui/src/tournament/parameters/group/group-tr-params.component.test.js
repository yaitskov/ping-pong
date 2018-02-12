import { setupAngularJs, ij } from 'test/angularjs-test-setup.js';
import GroupParamsCtrl from './GroupParamsCtrl.js';

describe('group-tr-params', () => {
    var initEventFired = false;

    const ctx = setupAngularJs(
        'group-tr-params',
        {onInit: s => s.$on(GroupParamsCtrl.readyEvent, e => initEventFired = true),
         parentCtrl: 'tournament-parameters-form'});

    it('ready event is emitted', () => {
        expect(initEventFired).toBeTrue();
    });


    const tournamentWithoutGroup = () => { return {tid: 1, rules: {}}; };
    const tournamentWithoutPlayOff = () => { return {tid: 1, rules: {group: {console: 'NO'}}}; };

    ij('group panel is not visible if tournament has no group', ($rootScope) => {
        $rootScope.$broadcast('event.tournament.rules.set', tournamentWithoutGroup());
        ctx.sync();
        expect(ctx.ctrl.useGroups).toBeFalse();
        expect(ctx.element.find('#group-parameters').hasClass('ng-hide')).toBeTrue();
        expect(ctx.element.find('#group-parameters-toggler input').prop('checked')).toBeFalse();
    });

    ij('group panel is visible if tournament has group', ($rootScope) => {
        $rootScope.$broadcast('event.tournament.rules.set', tournamentWithoutPlayOff());
        ctx.sync();
        expect(ctx.ctrl.useGroups).toBeTrue();
        expect(ctx.element.find('#group-parameters').hasClass('ng-hide')).toBeFalse();
        expect(ctx.element.find('#group-parameters-toggler input').prop('checked')).toBeTrue();
    });
});
