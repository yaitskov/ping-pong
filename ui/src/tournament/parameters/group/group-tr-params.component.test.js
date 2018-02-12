import { setupAngularJs, ij } from 'test/angularjs-test-setup.js';
import GroupParamsCtrl from './GroupParamsCtrl.js';
import defaultTournamentRules from 'tournament/new/defaultTournamentRules.js';

describe('group-tr-params', () => {
    var initEventFired = false;

    const ctx = setupAngularJs(
        'group-tr-params',
        {onInit: s => s.$on(GroupParamsCtrl.readyEvent, e => initEventFired = true),
         parentCtrl: 'tournament-parameters-form'});

    it('ready event is emitted', () => {
        expect(initEventFired).toBeTrue();
    });

    const tournamentWithoutGroup = () => { return {rules: {}}; };
    const tournamentWithPlayOff = () => { return {rules: defaultTournamentRules('PingPong')}; };

    ij('group panel is not visible if tournament has no group', ($rootScope) => {
        $rootScope.$broadcast('event.tournament.rules.set', tournamentWithoutGroup());
        expect(ctx.ctrl.useGroups).toBeFalse();
        expect(ctx.element.find('#group-parameters').hasClass('ng-hide')).toBeTrue();
        expect(ctx.element.find('#group-parameters-toggler input').prop('checked')).toBeFalse();
    });

    ij('group panel is visible if tournament has group', ($rootScope) => {
        $rootScope.$broadcast('event.tournament.rules.set', tournamentWithPlayOff());
        ctx.sync();
        expect(ctx.ctrl.useGroups).toBeTrue();
        expect(ctx.element.find('#group-parameters').hasClass('ng-hide')).toBeFalse();
        expect(ctx.element.find('#group-parameters-toggler input').prop('checked')).toBeTrue();
    });

    ij('disambiguate strategy toggles', ($rootScope) => {
        $rootScope.$broadcast('event.tournament.rules.set', tournamentWithPlayOff());
        expect(ctx.ctrl.rules.group.disambiguation).toBe('CMP_WIN_MINUS_LOSE');
        ctx.element.find('#disambiguate-strategy .btn-primary').click();
        ctx.sync();
        expect(ctx.ctrl.rules.group.disambiguation).toBe('CMP_WIN_MINUS_LOSE');
        ctx.element.find('#disambiguate-strategy :not(.btn-primary)').click();
        ctx.sync();
        expect(ctx.ctrl.rules.group.disambiguation).toBe('CMP_WIN_AND_LOSE');
        ctx.element.find('#disambiguate-strategy :not(.btn-primary)').click();
        ctx.sync();
        expect(ctx.ctrl.rules.group.disambiguation).toBe('CMP_WIN_MINUS_LOSE');
    });

    ij('how much quits group and its size are visible if tournament has play off', ($rootScope) => {
        $rootScope.$broadcast('event.tournament.rules.set', tournamentWithPlayOff());
        ctx.sync();
        expect(ctx.element.find('#how-much-quits-group').hasClass('ng-hide')).toBeFalse();
        expect(ctx.element.find('#max-group-size').hasClass('ng-hide')).toBeFalse();
    });

    ij('how much quits group and its size are not visible if tournament has no play off', ($rootScope) => {
        const tournament = tournamentWithPlayOff();
        delete tournament.rules.playOff;
        $rootScope.$broadcast('event.tournament.rules.set', tournament);
        ctx.sync();
        expect(ctx.element.find('#how-much-quits-group').hasClass('ng-hide')).toBeTrue();
        expect(ctx.element.find('#max-group-size').hasClass('ng-hide')).toBeTrue();
    });

    ij('quits group toggles', ($rootScope) => {
        $rootScope.$broadcast('event.tournament.rules.set', tournamentWithPlayOff());
        ctx.sync();
        expect(ctx.ctrl.rules.group.quits).toBe(2);
        ctx.element.find('#how-much-quits-group .btn-success').click();
        ctx.sync();
        expect(ctx.ctrl.rules.group.quits).toBe(2);
        ctx.element.find('#how-much-quits-group a:not(.btn-success)').click();
        ctx.sync();
        expect(ctx.ctrl.rules.group.quits).toBe(1);
        ctx.element.find('#how-much-quits-group a:not(.btn-primary)').click();
        ctx.sync();
        expect(ctx.ctrl.rules.group.quits).toBe(2);
    });

    ij('enable group panel', ($rootScope) => {
        $rootScope.$broadcast('event.tournament.rules.set', tournamentWithoutGroup());
        ctx.element.find('#group-parameters-toggler input').bootstrapToggle('on');
        ctx.sync();
        expect(ctx.ctrl.rules.group.groupSize).toBe(9);
        expect(ctx.ctrl.rules.group.quits).toBe(1);
    });
});
