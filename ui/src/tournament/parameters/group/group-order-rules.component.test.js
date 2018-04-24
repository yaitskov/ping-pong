import { setupAngularJs } from 'test/angularjs-test-setup.js';
import GroupOrderRulesCtrl from './GroupOrderRulesCtrl.js';
import { newTournament } from 'test/defaultTournaments.js';

describe('group-order-rules', () => {
    const ctx = setupAngularJs('group-order-rules');

    it('translate group order rule label', () => {
        ctx.send(GroupOrderRulesCtrl.TopicTournamentRulesAvailable,
                 newTournament('PingPong').rules);

        expect(ctx.find('a.group-rule-label:first').text()).toBe('Score Sum');
    });
});

