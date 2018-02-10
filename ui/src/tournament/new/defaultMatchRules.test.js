import defaultMatchRules from './defaultMatchRules.js';

describe('defaultMatchRules', () => {
    it('for tennis', () => {
        expect(defaultMatchRules('Tennis').minGamesToWin).toBe(6);
    });
    it('for ping-pong', () => {
        expect(defaultMatchRules('PingPong').minGamesToWin).toBe(11);
    });
    it('exception for unknown sport', () => {
        expect(() => defaultMatchRules('Badminton')).
            toThrow(new Error('Sport [Badminton] is not supported'));
    });
});
