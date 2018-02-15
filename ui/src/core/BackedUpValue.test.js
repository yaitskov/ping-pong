import backedUpValue from './BackedUpValue.js';

describe('BackedUpValue', () => {
    it('disable / enable', () => {
        let x = {a: 2};
        const backup = backedUpValue(() => 1, () => x.a);
        expect(x.a = backup.map(false)).toBeUndefined();
        expect(backup.map(true)).toBe(2);
    });
    it('enable default', () => {
        let x = {};
        const backup = backedUpValue(() => 1, () => x.a);
        expect(x.a = backup.map(true)).toBe(1);
    });
    it('enable default', () => {
        let x = {a: 2};
        const backup = backedUpValue(() => 1, () => x.a);
        expect(x.a = backup.map(true)).toBe(2);
        expect(backup.backup).toBeNull();
    });
    it('keep on undefined', () => {
        let x = {a: 2};
        const backup = backedUpValue(() => 1, () => x.a);
        expect(x.a = backup.map(undefined)).toBe(2);
    });
});
