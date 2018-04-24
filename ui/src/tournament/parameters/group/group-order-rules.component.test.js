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
});

