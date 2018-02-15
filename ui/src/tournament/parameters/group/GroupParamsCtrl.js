import BaseTrParamsCtrl from 'tournament/parameters/BaseTrParamsCtrl.js';
import backedUpValue from 'core/backedUpValue.js';

function defaultGroupRules() {
    return {
        quits: 1,
        groupSize: 9,
        disambiguation: 'CMP_WIN_MINUS_LOSE',
        console: 'NO',
        schedule: {
            size2Schedule: {2: [0, 1]}
        }
    };
}

export default class GroupParamsCtrl extends BaseTrParamsCtrl {
    static get $inject() {
        return ['requestStatus', 'groupSchedule'].concat(super.$inject);
    }

    get isValid() {
         if (!this.rules.group) {
             return true;
         }
         if (!this.groupScheduleIsValid) {
             return false;
         }
         if (this.rules.group.groupSize <= this.rules.group.quits) {
             this.requestStatus.validationFailed('group-size-less-quits');
             return false;
         }
         return true;
    }

    watchForUseGroups() {
        this.$scope.$watch('$ctrl.useGroups', (newValue, oldValue) => {
            console.log(`useGroups change ${newValue} ${oldValue}`);
            this.rules.group = this.groupRuleBackup.map(newValue);
            this.generateGroupSchedule();
        });
    }

    generateGroupSchedule() {
        const group = this.rules.group;
        if (group) {
            this.groupScheduleJson = this.groupSchedule.convertToText(group.schedule.size2Schedule);
        } else {
            this.groupScheduleJson = '';
        }
    }

    get groupScheduleIsValid() {
        if (!this.rules) {
            return true;
        }
        try {
            this.groupScheduleErrors = [];
            console.log(`parseText [${this.groupScheduleJson}]`);
            const schedule = this.groupSchedule.parseText(this.groupScheduleJson);
            if (schedule) {
                this.rules.group.schedule = {size2Schedule: schedule};
            } else {
                this.rules.group.schedule = null;
            }
            return true;
        } catch (e) {
            this.groupScheduleErrors.push(e);
            return false;
        }
    }

    onTournamentSet(tournament) {
        super.onTournamentSet(tournament);
        this.watchForUseGroups();
        const group = this.rules.group;
        this.useGroups = !!group;
        if (!group) {
            return;
        }
        if (!group.schedule || !group.schedule.size2Schedule) {
            group.schedule = defaultGroupRules().schedule;
        }
        this.generateGroupSchedule();
    }

    constructor() {
        super(...arguments);
        this.maxGroupSize = {min: 2, max: 20};
        this.groupRuleBackup = backedUpValue(defaultGroupRules, () => this.rules.group);
        this.useGroups = false;
        this.groupScheduleErrors = [];
        this.formatScheduleError = this.groupSchedule.formatScheduleError;
    }
}
