import { setupAngularJs } from 'test/angularjs-test-setup.js';
import GroupOrderRulesCtrl from './GroupOrderRulesCtrl.js';
import { ruleId, createMatchBasedRule, createF2fRule } from './rules.js';
import GroupRuleParametersDialog from './GroupRuleParametersDialog.js';


describe('group-rule-parameters-dialog', () => {
    const ctx = setupAngularJs('group-rule-parameters-dialog');

    it('hidden by default', () => () => ctx.hidden('#group-rule-parameters'));

    describe('with punkts rule', () => {
        const setRules = () => ctx.send(GroupRuleParametersDialog.TopicLoad,
                                        createMatchBasedRule(ruleId.Punkts));

        beforeEach(() => setRules());

        it('dialog visible', () => ctx.visible('#group-rule-parameters'));

        it('translate title',
           () => expect(ctx.find('h4.modal-title:first').text()).toBe(
               'Parameters for Score Sum'));

        it('custom dm matches hidden', () => ctx.hidden('#custom-dm-match-rules-toggler'));

        it('match participant scope visible', () => ctx.visible('#match-participant-scope'));
        it('match outcome scope visible', () => ctx.visible('#match-outcome-scope'));
        it('match outcome scope keys',
           () => expect(ctx.ctrl.matchOutcomeScopeKeys.sort()).
           toEqual(['ALL_MATCHES', 'JUST_NORMALLY_COMPLETE']));
        it('match participant scope keys',
           () => expect(ctx.ctrl.matchParticipantScopeKeys.sort()).
           toEqual(['AT_LEAST_ONE', 'BOTH']));
    });
});
