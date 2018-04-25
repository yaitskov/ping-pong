import { setupAngularJs } from 'test/angularjs-test-setup.js';
import GroupOrderRulesCtrl from './GroupOrderRulesCtrl.js';
import { newTournament } from 'test/defaultTournaments.js';
import { ruleId } from './rules.js';

describe('group-order-rules', () => {
    const ctx = setupAngularJs('group-order-rules');

    const setRules = () => ctx.send(GroupOrderRulesCtrl.TopicTournamentRulesAvailable,
                                    newTournament('PingPong').rules);
    it('translate group order rule label', () => {
        setRules();
        expect(ctx.find('a.group-rule-label:first').text()).toBe('Score Sum');
    });

    it('first rule has visible menu by default', () => {
        setRules();
        ctx.visible('.group-rule:first .group-rule-menu');
    });

    it('second rule has hidden menu by default', () => {
        setRules();
        ctx.hidden('.group-rule:eq(1) .group-rule-menu');
    });

    it('click rule label toggles menu', () => {
        setRules();
        ctx.click('.group-rule-label:eq(1)');
        ctx.visible('.group-rule-menu:eq(1)');
        ctx.hidden('.group-rule-menu:eq(0)');
    });

    it('config button is hidden for rnd rule', () => {
        setRules();
        ctx.hidden('.group-order-rule-config:last');
    });

    it('config button is visible for punkts and f2f', () => {
        setRules();
        ctx.visible('.group-order-rule-config:first');
        ctx.visible('.group-order-rule-config:eq(1)');
    });

    it('remove button is hidden for last rule', () => {
        setRules();
        ctx.hidden('.group-order-rule-remove:last');
    });

    it('remove button is visible for first and before last rule', () => {
        setRules();
        ctx.visible('.group-order-rule-remove:first');
        ctx.visible('.group-order-rule-remove:eq(-2)');
    });

    it('click on remove button removes rule', () => {
        setRules();
        expect(ctx.ctrl.rules[0]['@type']).toBe(ruleId.Punkts);
        ctx.click('.group-order-rule-remove:first');
        expect(ctx.ctrl.rules[0]['@type']).toBe(ruleId.f2f);
    });

    it('rule remove keeps index of rule with visible menud', () => {
        setRules();
        ctx.click('.group-order-rule-remove:first');
        expect(ctx.ctrl.indexRuleWithMenu).toBe(0);
    });

    describe('move up button', () => {
        it('hidden in first rule', () => {
            setRules();
            ctx.hidden('.group-order-rule-up:first');
        });
        it('hidden in last rule', () => {
            setRules();
            ctx.hidden('.group-order-rule-up:last');
        });
        it('visible in second rule', () => {
            setRules();
            ctx.visible('.group-order-rule-up:eq(1)');
        });
        it('clicking moves rule up with menu', () => {
            setRules();
            ctx.ctrl.indexRuleWithMenu = 1;
            ctx.click('.group-order-rule-up:eq(1)');
            expect(ctx.ctrl.rules[0]['@type']).toBe(ruleId.f2f);
            expect(ctx.ctrl.rules[1]['@type']).toBe(ruleId.Punkts);
            expect(ctx.ctrl.indexRuleWithMenu).toBe(0);
        });
    });

    it('set rules reset index', () => {
        setRules();
        ctx.ctrl.indexRuleWithMenu = 2;
        setRules();
        expect(ctx.ctrl.indexRuleWithMenu).toBe(0);
    });

    describe('move down button', () => {
        it('hidden in before last rule', () => {
            setRules();
            ctx.hidden('.group-order-rule-down:eq(-2)');
        });
        it('hidden in last rule', () => {
            setRules();
            ctx.hidden('.group-order-rule-down:last');
        });
        it('visible in first rule', () => {
            setRules();
            ctx.visible('.group-order-rule-down:first');
        });
        it('clicking moves rule down with menu', () => {
            setRules();
            ctx.click('.group-order-rule-down:first');
            expect(ctx.ctrl.rules[0]['@type']).toBe(ruleId.f2f);
            expect(ctx.ctrl.rules[1]['@type']).toBe(ruleId.Punkts);
            expect(ctx.ctrl.indexRuleWithMenu).toBe(1);
        });
    });
});
