import { setupAngularJs } from 'test/angularjs-test-setup.js';
import { checkTouchSpinIncrease, checkTouchSpinDecrease } from 'test/touchSpin.js';
import GroupParamsCtrl from './GroupParamsCtrl.js';
import { newTournamentWithGroup, newTournamentWithoutGroup, newTournamentWithoutPlayOff,
         existingTournamentWithConsole } from 'test/defaultTournaments.js';

describe('group-tr-params', () => {
    var initEventFired = false;

    const ctx = setupAngularJs(
        'group-tr-params',
        {onInit: s => s.$on(GroupParamsCtrl.readyEvent, e => initEventFired = true),
         parentCtrl: 'tournament-parameters-form'});

    it('ready event is emitted', () => {
        expect(initEventFired).toBeTrue();
    });

    it('group panel is not visible if tournament has no group', () => {
        ctx.broadcast('event.tournament.rules.set', newTournamentWithoutGroup());
        expect(ctx.ctrl.useGroups).toBeFalse();
        ctx.hidden('#group-parameters');
        ctx.unchecked('#group-parameters-toggler input');
    });

    it('group panel is visible if tournament has group', () => {
        ctx.broadcast('event.tournament.rules.set', newTournamentWithGroup());
        expect(ctx.ctrl.useGroups).toBeTrue();
        ctx.visible('#group-parameters');
        ctx.checked('#group-parameters-toggler input');
    });

    it('disambiguate strategy toggles', () => {
        ctx.broadcast('event.tournament.rules.set', newTournamentWithGroup());
        ctx.btnToggles('#disambiguate-strategy',
                       () => ctx.ctrl.rules.group.disambiguation,
                       {default: {value: 'CMP_WIN_MINUS_LOSE', clazz: 'btn-primary'},
                        other: {value: 'CMP_WIN_AND_LOSE', clazz: 'btn-primary'}});
    });

    it('how much quits group and its size are visible if tournament has play off', () => {
        ctx.broadcast('event.tournament.rules.set', newTournamentWithGroup());
        ctx.visible('#how-much-quits-group');
        ctx.visible('#max-group-size');
    });

    it('how much quits group and its size are not visible if tournament has no play off', () => {
        ctx.broadcast('event.tournament.rules.set', newTournamentWithoutPlayOff());
        ctx.hidden('#how-much-quits-group');
        ctx.hidden('#max-group-size');
    });

    it('quits group toggles', () => {
        ctx.broadcast('event.tournament.rules.set', newTournamentWithGroup());
        ctx.btnToggles('#how-much-quits-group',
                       () => ctx.ctrl.rules.group.quits,
                       {default: {clazz: 'btn-success', value: 2},
                        other: {clazz: 'btn-primary', value: 1}});
    });

    it('enable group panel', () => {
        ctx.broadcast('event.tournament.rules.set', newTournamentWithoutGroup());
        ctx.toggleOn('#group-parameters-toggler input');
        ctx.sync();
        expect(ctx.ctrl.rules.group.groupSize).toBe(9);
        expect(ctx.ctrl.rules.group.quits).toBe(1);
    });

    it('enable disabled group panel', () => {
        ctx.broadcast('event.tournament.rules.set', newTournamentWithGroup());
        ctx.toggleOff('#group-parameters-toggler input');
        expect(ctx.ctrl.rules.group).toBeUndefined();
        ctx.toggleOn('#group-parameters-toggler input');
        expect(ctx.ctrl.rules.group.groupSize).toBe(8);
        expect(ctx.ctrl.rules.group.quits).toBe(2);
    });

    it('max group size binding', () => {
        ctx.broadcast('event.tournament.rules.set', newTournamentWithGroup());
        expect(ctx.element.find('#max-group-size input').val()).toBe('8');
    });

    it('button up increase group size', () => {
        ctx.broadcast('event.tournament.rules.set', newTournamentWithGroup());
        checkTouchSpinIncrease(ctx, '#max-group-size', () => ctx.ctrl.rules.group.groupSize);
    });

    it('button down decrease group size', () => {
        ctx.broadcast('event.tournament.rules.set', newTournamentWithGroup());
        checkTouchSpinDecrease(ctx, '#max-group-size', () => ctx.ctrl.rules.group.groupSize);
    });

    it('translate works for max group size help', () => {
        expect(ctx.element.find('#max-group-size p').text()).
            toBe('Maximum number of players, which could be in a group.');
    });

    it('schedule is serialized to JSON on set rules', () => {
        const tournament = newTournamentWithGroup();
        tournament.rules.group.schedule.size2Schedule = {2: [0, 1]};
        ctx.broadcast('event.tournament.rules.set', tournament);
        expect(ctx.ctrl.groupScheduleJson).toBe("2: 1-2\n");
        expect(ctx.element.find('textarea[name=groupSchedule]').val()).toBe("2: 1-2\n");
    });

    it('schedule is serialized to JSON on rules restore', () => {
        const tournament = newTournamentWithGroup();
        tournament.rules.group.schedule.size2Schedule = {2: [0, 1]};
        ctx.broadcast('event.tournament.rules.set', tournament);
        ctx.toggleOff('#group-parameters-toggler input');
        ctx.toggleOn('#group-parameters-toggler input');
        expect(ctx.ctrl.groupScheduleJson).toBe("2: 1-2\n");
        expect(ctx.element.find('textarea[name=groupSchedule]').val()).toBe("2: 1-2\n");
    });

    it('schedule is updated from JSON on validate', () => {
        const tournament = newTournamentWithGroup();
        ctx.broadcast('event.tournament.rules.set', tournament);
        ctx.element.find('textarea[name=groupSchedule]').val("2: 2-1\n").trigger('change');
        ctx.sync();
        expect(ctx.ctrl.groupScheduleJson).toBe("2: 2-1");
        expect(ctx.ctrl.groupScheduleIsValid).toBeTrue();
        expect(ctx.ctrl.rules.group.schedule.size2Schedule).toEqual({2: [1, 0]});
    });

    it('validation pass for tournament without group', () => {
        ctx.broadcast('event.tournament.rules.set', newTournamentWithoutGroup());
        expect(ctx.ctrl.isValid).toBeTrue();
    });

    it('validation fails due group size <= quits', () => {
        const tournament = existingTournamentWithConsole();
        tournament.rules.group = {...tournament.rules.group, quits:3, groupSize:3};
        ctx.broadcast('event.tournament.rules.set', tournament);
        expect(ctx.ctrl.isValid).toBeFalse();
    });

    it('validation fails due group schedule has sytax error', () => {
        ctx.broadcast('event.tournament.rules.set', newTournamentWithGroup());
        ctx.ctrl.groupScheduleJson = "2: 1";
        expect(ctx.ctrl.groupScheduleIsValid).toBeFalse();
        expect(ctx.ctrl.isValid).toBeFalse();
    });
});
