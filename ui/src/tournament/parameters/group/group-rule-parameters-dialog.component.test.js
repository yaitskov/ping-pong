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

        it('translate title',
           () => expect(ctx.find('h4.modal-title:first').text()).toBe(
               'Parameters for Score Sum'));
    });

    // it('current rule button is outlined', () => {
    //     ctx.hasClass('.group-rule-label:first', 'btn-primary');
    //     ctx.hasNoClass('.group-rule-label:eq(1)', 'btn-primary');
    //     ctx.click('.group-rule-label:eq(1)');
    //     ctx.hasNoClass('.group-rule-label:first', 'btn-primary');
    //     ctx.hasClass('.group-rule-label:eq(1)', 'btn-primary');
    // });

    // describe('add rule button above the current one', () => {
    //     it('click emits show available rules with sport', () => {
    //         ctx.recordEvents(PickGroupRulesDialog.TopicShowAvailableRules, (events) => {
    //             ctx.click('.group-order-rule-add:first');
    //             expect(events[0][0]).toBe('PingPong');
    //         });
    //     });
    // });

    // describe('config button', () => {
    //     describe('visibility', () => {
    //         it('hidden for rnd rule', () => ctx.hidden('.group-order-rule-config:last'));
    //         it('visible for punkts', () => ctx.visible('.group-order-rule-config:first'));
    //         it('visible for f2f', () => ctx.visible('.group-order-rule-config:eq(1)'));
    //     });
    //     it('click emits topic load and show dialog', () => {
    //         ctx.recordEvents(GroupRuleParametersDialog.TopicLoad, (events) => {
    //             ctx.click('.group-order-rule-config:first');
    //             expect(events[0][0]['@type']).toBe(ruleId.Punkts);
    //             expect(events[0][1]['@type']).toBe('PingPong');
    //         });
    //     });
    // });

    // describe('remove button', () => {
    //     it('hidden for last rule',  () => ctx.hidden('.group-order-rule-remove:last'));
    //     it('visible in first rule', () => ctx.visible('.group-order-rule-remove:first'));
    //     it('visible in before last rule',
    //        () => ctx.visible('.group-order-rule-remove:eq(-2)'));

    //     it('click removes rule', () => {
    //         expect(ctx.ctrl.rules[0]['@type']).toBe(ruleId.Punkts);
    //         ctx.click('.group-order-rule-remove:first');
    //         expect(ctx.ctrl.rules[0]['@type']).toBe(ruleId.f2f);
    //     });

    //     it('rule remove keeps index of rule with visible menud', () => {
    //         ctx.click('.group-order-rule-remove:first');
    //         expect(ctx.ctrl.indexRuleWithMenu).toBe(0);
    //     });
    // });

    // describe('move up button', () => {
    //     it('hidden in first rule', () => ctx.hidden('.group-order-rule-up:first'));
    //     it('hidden in last rule', () => ctx.hidden('.group-order-rule-up:last'));
    //     it('visible in second rule', () => ctx.visible('.group-order-rule-up:eq(1)'));
    //     it('clicking moves rule up with menu', () => {
    //         ctx.ctrl.indexRuleWithMenu = 1;
    //         ctx.click('.group-order-rule-up:eq(1)');
    //         expect(ctx.ctrl.rules[0]['@type']).toBe(ruleId.f2f);
    //         expect(ctx.ctrl.rules[1]['@type']).toBe(ruleId.Punkts);
    //         expect(ctx.ctrl.indexRuleWithMenu).toBe(0);
    //     });
    // });

    // it('set rules reset index', () => {
    //     ctx.ctrl.indexRuleWithMenu = 2;
    //     setRules();
    //     expect(ctx.ctrl.indexRuleWithMenu).toBe(0);
    // });

    // describe('move down button', () => {
    //     it('hidden in before last rule', () => ctx.hidden('.group-order-rule-down:eq(-2)'));
    //     it('hidden in last rule', () => ctx.hidden('.group-order-rule-down:last'));
    //     it('visible in first rule', () => ctx.visible('.group-order-rule-down:first'));
    //     it('clicking moves rule down with menu', () => {
    //         ctx.click('.group-order-rule-down:first');
    //         expect(ctx.ctrl.rules[0]['@type']).toBe(ruleId.f2f);
    //         expect(ctx.ctrl.rules[1]['@type']).toBe(ruleId.Punkts);
    //         expect(ctx.ctrl.indexRuleWithMenu).toBe(1);
    //     });
    // });
});
