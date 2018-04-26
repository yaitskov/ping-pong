import { setupAngularJs } from 'test/angularjs-test-setup.js';
import GroupOrderRulesCtrl from './GroupOrderRulesCtrl.js';
import { newTournament } from 'test/defaultTournaments.js';
import { ruleId } from './rules.js';
import PickGroupRulesDialog from './PickGroupRulesDialog.js';
import GroupRuleParametersDialog from './GroupRuleParametersDialog.js';


describe('group-order-rules', () => {
    const ctx = setupAngularJs('group-order-rules');

    const setRules = () => ctx.send(GroupOrderRulesCtrl.TopicTournamentRulesAvailable,
                                    newTournament('PingPong').rules);
    beforeEach(() => setRules());

    describe('menu', () => {
        it('translate group order rule label',
           () => expect(ctx.find('a.group-rule-label:first').text()).toBe('Score Sum'));
        it('first rule has visible menu by default',
           () => ctx.visible('.group-rule:first .group-rule-menu'));
        it('second rule has hidden menu by default',
           () => ctx.hidden('.group-rule:eq(1) .group-rule-menu'));
        it('click rule label toggles menu', () => {
            ctx.click('.group-rule-label:eq(1)');
            ctx.visible('.group-rule-menu:eq(1)');
            ctx.hidden('.group-rule-menu:eq(0)');
        });
    });

    describe('config button', () => {
        describe('visibility', () => {
            it('hidden for rnd rule', () => ctx.hidden('.group-order-rule-config:last'));
            it('visible for punkts', () => ctx.visible('.group-order-rule-config:first'));
            it('visible for f2f', () => ctx.visible('.group-order-rule-config:eq(1)'));
        });
        it('click emits topic load and show dialog', () => {
            ctx.recordEvents(GroupRuleParametersDialog.TopicLoad, (events) => {
                ctx.click('.group-order-rule-config:first');
                ctx.sync();
                expect(events[0][0]['@type']).toBe(ruleId.Punkts);
                expect(events[0][1]['@type']).toBe('PingPong');
            });
        });
    });

    describe('remove button', () => {
        it('hidden for last rule',  () => ctx.hidden('.group-order-rule-remove:last'));
        it('visible in first rule', () => ctx.visible('.group-order-rule-remove:first'));
        it('visible in before last rule',
           () => ctx.visible('.group-order-rule-remove:eq(-2)'));

        it('click removes rule', () => {
            expect(ctx.ctrl.rules[0]['@type']).toBe(ruleId.Punkts);
            ctx.click('.group-order-rule-remove:first');
            expect(ctx.ctrl.rules[0]['@type']).toBe(ruleId.f2f);
        });

        it('rule remove keeps index of rule with visible menud', () => {
            ctx.click('.group-order-rule-remove:first');
            expect(ctx.ctrl.indexRuleWithMenu).toBe(0);
        });
    });

    describe('move up button', () => {
        it('hidden in first rule', () => ctx.hidden('.group-order-rule-up:first'));
        it('hidden in last rule', () => ctx.hidden('.group-order-rule-up:last'));
        it('visible in second rule', () => ctx.visible('.group-order-rule-up:eq(1)'));
        it('clicking moves rule up with menu', () => {
            ctx.ctrl.indexRuleWithMenu = 1;
            ctx.click('.group-order-rule-up:eq(1)');
            expect(ctx.ctrl.rules[0]['@type']).toBe(ruleId.f2f);
            expect(ctx.ctrl.rules[1]['@type']).toBe(ruleId.Punkts);
            expect(ctx.ctrl.indexRuleWithMenu).toBe(0);
        });
    });

    it('set rules reset index', () => {
        ctx.ctrl.indexRuleWithMenu = 2;
        setRules();
        expect(ctx.ctrl.indexRuleWithMenu).toBe(0);
    });

    describe('move down button', () => {
        it('hidden in before last rule', () => ctx.hidden('.group-order-rule-down:eq(-2)'));
        it('hidden in last rule', () => ctx.hidden('.group-order-rule-down:last'));
        it('visible in first rule', () => ctx.visible('.group-order-rule-down:first'));
        it('clicking moves rule down with menu', () => {
            ctx.click('.group-order-rule-down:first');
            expect(ctx.ctrl.rules[0]['@type']).toBe(ruleId.f2f);
            expect(ctx.ctrl.rules[1]['@type']).toBe(ruleId.Punkts);
            expect(ctx.ctrl.indexRuleWithMenu).toBe(1);
        });
    });
});
