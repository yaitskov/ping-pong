import SimpleController from 'core/angular/SimpleController.js';
import PickGroupRulesDialog from './PickGroupRulesDialog.js';
import GroupRuleParametersDialog from './GroupRuleParametersDialog.js';
import GroupRules from './rules.js';

export default class GroupOrderRulesCtrl extends SimpleController {
    static get TopicTournamentRulesAvailable() {
        return 'tournament-rules-available';
    }

    $onInit() {
        this.subscribe(this.constructor.TopicTournamentRulesAvailable,
                       (e) => this.setRules(e));
        this.subscribe(PickGroupRulesDialog.TopicRuleSelected,
                       (ruleId) => this.onRuleSelected(ruleId));
        this.indexRuleWithMenu = 0;
    }

    hasParameters(rule) {
        return rule['@type'] != GroupRules.ruleId.f2f;
    }

    onRuleSelected(ruleId) {
        this.rules.splice(this.indexRuleWithMenu, 0, GroupRules.ruleType2Factory(ruleId)());
    }

    setRules(tournamentRules) {
        this.rules = tournamentRules.group.orderRules;
        this.sport = tournamentRules.match['@type'];
        this.commonMatchRules = tournamentRules.match;
    }

    showMenu(index) {
        this.indexRuleWithMenu = index;
    }

    configureRule(groupOrderRule) {
        this.send(GroupRuleParametersDialog.TopicLoad,
                  groupOrderRule,
                  this.commonMatchRules);
    }

    moveItem(index, shift) {
        const gor = this.rules.splice(index, 1);
        this.rules.splice(index + shift, gor);
        this.indexRuleWithMenu += shift;
    }

    addRule($index) {
        this.send(PickGroupRulesDialog.TopicShowAvailableRules, this.sport);
    }

    removeRule($index) {
        this.rules.splice($index, 1);
    }
}
