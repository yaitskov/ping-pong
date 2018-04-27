import { setupAngularJs } from 'test/angularjs-test-setup.js';
import GroupOrderRulesCtrl from './GroupOrderRulesCtrl.js';
import { ruleId, createMatchBasedRule, createF2fRule, createDmRule } from './rules.js';
import GroupRuleParametersDialog from './GroupRuleParametersDialog.js';
import defaultMatchRules from 'tournament/new/defaultMatchRules.js';


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
        it('translate match outcome scope', () =>
           ctx.transAs('#match-outcome-scope label:first', 'Count broken matches'));
        it('translate participant scope', () =>
           ctx.transAs('#match-participant-scope label:first',
                       'Count only matches among competing players'));
    });

    describe('with f2f rule', () => {
        const setRules = () => ctx.send(GroupRuleParametersDialog.TopicLoad,
                                        createF2fRule());

        beforeEach(() => setRules());

        it('translate title',
           () => expect(ctx.find('h4.modal-title:first').text()).toBe(
               'Parameters for Face-to-Face'));
        it('custom dm matches hidden', () => ctx.hidden('#custom-dm-match-rules-toggler'));
        it('match participant scope hidden', () => ctx.hidden('#match-participant-scope'));
        it('match outcome scope visible', () => ctx.visible('#match-outcome-scope'));
    });

    describe('with DM rule', () => {
        const setRules = () => ctx.send(GroupRuleParametersDialog.TopicLoad,
                                        createDmRule(), defaultMatchRules('PingPong'));

        beforeEach(() => setRules());

        it('custom dm matches toggler visible', () => ctx.visible('#custom-dm-match-rules-toggler'));
        it('custom dm matches panel hidden', () => ctx.hidden('#custom-dm-match-rules-panel'));
        it('match participant scope hidden', () => ctx.hidden('#match-participant-scope'));
        it('match outcome scope hidden', () => ctx.hidden('#match-outcome-scope'));

        it('toggle on custom dm rules', () => {
            ctx.toggleOn('#custom-dm-match-rules-toggler input');
            ctx.visible('#custom-dm-match-rules-panel');
            expect(ctx.ctrl.rule.match.setsToWin).toBe(3);
        });


        it('toggle off custom dm rules', () => {
            ctx.toggleOn('#custom-dm-match-rules-toggler input');
            ctx.toggleOff('#custom-dm-match-rules-toggler input');
            ctx.hidden('#custom-dm-match-rules-panel');
            expect(ctx.ctrl.rule.match).toBeUndefined();
        });

        it('save button emits updated rule', () => {
            ctx.toggleOn('#custom-dm-match-rules-toggler input');
            ctx.recordEvents(GroupRuleParametersDialog.TopicSave, (events) => {
                ctx.click('#save-group-rule-params-button');
                it('dialog hidden', () => ctx.hidden('#group-rule-parameters'));
                expect(events[0][0].match.setsToWin).toBe(3);
            });
        });
    });
});
