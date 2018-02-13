import { setupAngularJs } from 'test/angularjs-test-setup.js';
import { checkTouchSpinIncrease, checkTouchSpinDecrease } from 'test/touchSpin.js';
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

    it('group panel is not visible if tournament has no group', () => {
        ctx.broadcast('event.tournament.rules.set', tournamentWithoutGroup());
        expect(ctx.ctrl.useGroups).toBeFalse();
        ctx.hidden('#group-parameters');
        ctx.unchecked('#group-parameters-toggler input');
    });

    it('group panel is visible if tournament has group', () => {
        ctx.broadcast('event.tournament.rules.set', tournamentWithPlayOff());
        expect(ctx.ctrl.useGroups).toBeTrue();
        ctx.visible('#group-parameters');
        ctx.checked('#group-parameters-toggler input');
    });

    it('disambiguate strategy toggles', () => {
        ctx.broadcast('event.tournament.rules.set', tournamentWithPlayOff());
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

    it('how much quits group and its size are visible if tournament has play off', () => {
        ctx.broadcast('event.tournament.rules.set', tournamentWithPlayOff());
        ctx.visible('#how-much-quits-group');
        ctx.visible('#max-group-size');
    });

    it('how much quits group and its size are not visible if tournament has no play off', () => {
        const tournament = tournamentWithPlayOff();
        delete tournament.rules.playOff;
        ctx.broadcast('event.tournament.rules.set', tournament);
        ctx.hidden('#how-much-quits-group');
        ctx.hidden('#max-group-size');
    });

    it('quits group toggles', () => {
        ctx.broadcast('event.tournament.rules.set', tournamentWithPlayOff());
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

    it('enable group panel', () => {
        ctx.broadcast('event.tournament.rules.set', tournamentWithoutGroup());
        ctx.element.find('#group-parameters-toggler input').bootstrapToggle('on');
        ctx.sync();
        expect(ctx.ctrl.rules.group.groupSize).toBe(9);
        expect(ctx.ctrl.rules.group.quits).toBe(1);
    });

    it('enable disabled group panel', () => {
        ctx.broadcast('event.tournament.rules.set', tournamentWithPlayOff());
        ctx.element.find('#group-parameters-toggler input').bootstrapToggle('off');
        ctx.sync();
        expect(ctx.ctrl.groupRuleBackup.groupSize).toBe(8);
        expect(ctx.ctrl.groupRuleBackup.quits).toBe(2);
        ctx.element.find('#group-parameters-toggler input').bootstrapToggle('on');
        ctx.sync();
        expect(ctx.ctrl.rules.group.groupSize).toBe(8);
        expect(ctx.ctrl.rules.group.quits).toBe(2);
    });

    it('max group size binding', () => {
        ctx.broadcast('event.tournament.rules.set', tournamentWithPlayOff());
        expect(ctx.element.find('#max-group-size input').val()).toBe('8');
    });

    it('button up increase group size', () => {
        ctx.broadcast('event.tournament.rules.set', tournamentWithPlayOff());
        checkTouchSpinIncrease(ctx, '#max-group-size', () => ctx.ctrl.rules.group.groupSize);
    });

    it('button down decrease group size', () => {
        ctx.broadcast('event.tournament.rules.set', tournamentWithPlayOff());
        checkTouchSpinDecrease(ctx, '#max-group-size', () => ctx.ctrl.rules.group.groupSize);
    });

    it('translate works for max group size help', () => {
        expect(ctx.element.find('#max-group-size p').text()).
            toBe('Maximum number of players, which could be in a group.');
    });

    it('schedule is serialized to JSON on set rules', () => {
        const tournament = tournamentWithPlayOff();
        tournament.rules.group.schedule.size2Schedule = {2: [0, 1]};
        ctx.broadcast('event.tournament.rules.set', tournament);
        expect(ctx.ctrl.groupScheduleJson).toBe("2: 1-2\n");
        expect(ctx.element.find('textarea[name=groupSchedule]').val()).toBe("2: 1-2\n");
    });

    it('schedule is serialized to JSON on rules restore', () => {
        const tournament = tournamentWithPlayOff();
        tournament.rules.group.schedule.size2Schedule = {2: [0, 1]};
        ctx.broadcast('event.tournament.rules.set', tournament);
        ctx.element.find('#group-parameters-toggler input').bootstrapToggle('off');
        ctx.sync();
        ctx.element.find('#group-parameters-toggler input').bootstrapToggle('on');
        ctx.sync();
        expect(ctx.ctrl.groupScheduleJson).toBe("2: 1-2\n");
        expect(ctx.element.find('textarea[name=groupSchedule]').val()).toBe("2: 1-2\n");
    });

    it('schedule is updated from JSON on validate', () => {
        const tournament = tournamentWithPlayOff();
        ctx.broadcast('event.tournament.rules.set', tournament);
        ctx.element.find('textarea[name=groupSchedule]').val("2: 2-1\n").trigger('change');
        ctx.sync();
        expect(ctx.ctrl.groupScheduleJson).toBe("2: 2-1");
        expect(ctx.ctrl.groupScheduleIsValid).toBeTrue();
        expect(ctx.ctrl.rules.group.schedule.size2Schedule).toEqual({2: [1, 0]});
    });

    it('validation pass for tournament without group', () => {
        ctx.broadcast('event.tournament.rules.set', tournamentWithoutGroup());
        expect(ctx.ctrl.isValid).toBeTrue();
    });

    it('validation fails due group size <= quits', () => {
        ctx.broadcast('event.tournament.rules.set',
                      Object.assign(tournamentWithPlayOff(),
                                    {consoleTid: 3, rules: {group: {quits:3, groupSize:3}}}));
        expect(ctx.ctrl.isValid).toBeFalse();
    });

    it('validation fails due group schedule has sytax error', () => {
        ctx.broadcast('event.tournament.rules.set', tournamentWithPlayOff());
        ctx.ctrl.groupScheduleJson = "2: 1";
        expect(ctx.ctrl.groupScheduleIsValid).toBeFalse();
        expect(ctx.ctrl.isValid).toBeFalse();
    });
});
