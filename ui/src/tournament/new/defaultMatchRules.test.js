import defaultMatchRules from './defaultMatchRules.js';

describe('defaultMatchRules', () => {
    it('for tennis', () => {
        expect(defaultMatchRules('Tennis').mgtw).toBe(6);
    });
    it('for ping-pong', () => {
        expect(defaultMatchRules('PingPong').mgtw).toBe(11);
    });
    it('default sport is ping-pong', () => {
        expect(defaultMatchRules()['@type']).toBe('PingPong');
    });
    it('exception for unknown sport', () => {
        expect(() => defaultMatchRules('Badminton')).
            toThrow(new Error('Sport [Badminton] is not supported'));
    });
});
