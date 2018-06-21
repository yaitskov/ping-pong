import defaultMatchRules from './defaultMatchRules.js';

describe('defaultMatchRules', () => {
    it('for tennis', () => {
        expect(defaultMatchRules('TE').mgtw).toBe(6);
    });
    it('for ping-pong', () => {
        expect(defaultMatchRules('PP').mgtw).toBe(11);
    });
    it('default sport is ping-pong', () => {
        expect(defaultMatchRules()['@type']).toBe('PP');
    });
    it('exception for unknown sport', () => {
        expect(() => defaultMatchRules('Badminton')).
            toThrow(new Error('Sport [Badminton] is not supported'));
    });
});
