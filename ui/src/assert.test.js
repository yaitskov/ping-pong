const assert = require('assert');

describe('Demo', () => {
    it('should add correctly', () => {
        assert.equal(1 + 1, 2);
    });

    it('compare map', () => {
        var m1 = {};
        var m2 = {x: 1};
        m1['x'] = 1;
        assert.deepEqual(m1, m2);
    });

    it('compare map values', () => {
        var m = {a: 1};
        assert.deepEqual([1], Object.keys(m).map((k) => m[k]));
    });
});
