import { ruleId, ruleType2Factory } from './rules.js';

describe('group order rules', () => {
    describe('ruleId', () => {
        it('ruleId.WM == WM', () => expect(ruleId.WM).toBe('WM'));
    });

    describe('ruleType2Factory', () => {
        it('creates WM', () => expect(ruleType2Factory.get(ruleId.WM)()).toEqual(
            {
                '@type': 'WM',
                mps: 'alo',
                mos: 'am'
            }));
    });

});
