import backedUpValue from 'core/backedUpValue.js';
import SimpleDialog from 'core/angular/SimpleDialog.js';
import MatchOutcomeScope from './MatchOutcomeScope.js';
import MatchParticipantScope from './MatchParticipantScope.js';
import HeadlessDmMatchParams from './HeadlessDmMatchParamsCtrl.js';

export default class GroupRuleParametersDialog extends SimpleDialog {
    static get TopicLoad() {
        return 'group-rule-parameters-load';
    }

    static get TopicSave() {
        return 'group-rule-parameters-save';
    }

    $onInit() {
        this.dmMatchRulesBackup = backedUpValue(
            () => Object.assign({}, this.defaultMatchRules),
            () => this.rule.match);
        this.useCustomDmMatchRules = false;
        this.subscribe(GroupRuleParametersDialog.TopicLoad,
                       (gor, defaultMatchRules) => this.showDialogFor(gor, defaultMatchRules));
        this.MatchOutcomeScope = MatchOutcomeScope;
        this.MatchParticipantScope = MatchParticipantScope;
    }

    watchForUseDmMatchRules() {
        this.$scope.$watch('$ctrl.useCustomDmMatchRules', (newValue, oldValue) => {
            console.log(`useCustomDmMatchRules change ${newValue} ${oldValue}`);
            this.rule.match = this.dmMatchRulesBackup.map(newValue);
        });
    }

    showDialogFor(gor, defaultMatchRules) {
        this.originRule = gor;
        this.defaultMatchRules = defaultMatchRules;
        this.rule = Object.assign({}, gor);
        this.watchForUseDmMatchRules();
        this.useCustomDmMatchRules = !!this.rule.match;
        this.send(HeadlessDmMatchParams.TopicLoad, this.rule);
        this.showDialog('group-rule-parameters');
    }

    saveChanges() {
        this.send(GroupRuleParametersDialog.TopicSave, this.rule, this.originRule);
    }
}
