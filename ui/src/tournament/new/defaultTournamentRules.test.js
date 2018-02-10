import defaultTournamentRules from './defaultTournamentRules.js';

describe('defaultTournamentRules', () => {
    it('console is disable', () => {
        expect(defaultTournamentRules('Tennis').group.console).toBe('NO');
    });
});
