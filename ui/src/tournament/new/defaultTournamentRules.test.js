import defaultTournamentRules from './defaultTournamentRules.js';

describe('defaultTournamentRules', () => {
    it('console is disable', () => {
        expect(defaultTournamentRules('TE').group.console).toBe('NO');
    });
});
