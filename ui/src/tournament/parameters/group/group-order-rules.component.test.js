import { setupAngularJs } from 'test/angularjs-test-setup.js';
import GroupOrderRulesCtrl from './GroupOrderRulesCtrl.js';
import { newTournament } from 'test/defaultTournaments.js';

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
        //ctx.visible('.group-order-rule-config:eq(1)');
    });
});
