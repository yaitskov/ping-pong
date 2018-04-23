import SimpleDialog from 'core/angular/SimpleDialog.js';
import ruleIdsBySport from './ruleIdsBySport.js';

export default class PickGroupRulesDialog extends SimpleDialog {
    static get TopicShowAvailableRules() {
        return 'pick-group-rule-dialog-show-available-rules';
    }

    static get TopicRuleSelected() {
        return 'pick-group-rule-dialog-rule-selected';
    }

    $onInit() {
        this.subscribe(this.constructor.TopicShowAvailableRules,
                       (sport)  => this.showOptionsFor(sport));
    }

    showOptionsFor(sport) {
        this.ruleIds = ruleIdsBySport[sport];
        this.showDialog('rulesDialog');
    }

    pickThis(ruleId) {
        this.send(this.constructor.TopicRuleSelected, ruleId);
    }
}
