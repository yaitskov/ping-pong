import { setupAngularJs } from 'test/angularjs-test-setup.js';
import GroupOrderRulesCtrl from './GroupOrderRulesCtrl.js';
import { newTournament } from 'test/defaultTournaments.js';
import { ruleId } from './rules.js';
import PickGroupRulesDialog from './PickGroupRulesDialog.js';
import GroupRuleParametersDialog from './GroupRuleParametersDialog.js';


describe('group-order-rules', () => {
    const ctx = setupAngularJs('group-order-rules');

    const setRules = () => ctx.send(GroupOrderRulesCtrl.TopicTournamentRulesAvailable,
                                    newTournament('PP').rules);
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

    it('current rule button is outlined', () => {
        ctx.hasClass('.group-rule-label:first', 'btn-primary');
        ctx.hasNoClass('.group-rule-label:eq(1)', 'btn-primary');
        ctx.click('.group-rule-label:eq(1)');
        ctx.hasNoClass('.group-rule-label:first', 'btn-primary');
        ctx.hasClass('.group-rule-label:eq(1)', 'btn-primary');
    });

    describe('add rule button above the current one', () => {
        it('click emits show available rules with sport', () => {
            ctx.recordEvents(PickGroupRulesDialog.TopicShowAvailableRules, (events) => {
                ctx.click('.group-order-rule-add:first');
                expect(events[0][0]).toBe('PP');
            });
        });
    });

    describe('config button', () => {
        describe('visibility', () => {
            it('hidden for rnd rule', () => ctx.hidden('.group-order-rule-config:last'));
            it('visible for punkts', () => ctx.visible('.group-order-rule-config:first'));
            it('visible for dm', () => ctx.visible('.group-order-rule-config:eq(6)'));
            it('visible for f2f', () => ctx.visible('.group-order-rule-config:eq(1)'));
        });
        it('click emits topic load and show dialog', () => {
            ctx.recordEvents(GroupRuleParametersDialog.TopicLoad, (events) => {
                ctx.click('.group-order-rule-config:first');
                expect(events[0][0]['@type']).toBe(ruleId.Punkts);
                expect(events[0][1]['@type']).toBe('PP');
            });
        });
    });

    it('save event from group order rule params dialog updates rule', () => {
        const ruleCopy = Object.assign({}, ctx.ctrl.rules[0], {keyFromUpdate: 1});
        ctx.send(GroupRuleParametersDialog.TopicSave, ruleCopy, ctx.ctrl.rules[0]);
        expect(ctx.ctrl.rules[0].keyFromUpdate).toBe(1);
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
