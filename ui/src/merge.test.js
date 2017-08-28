const assert = require('assert');
import merge from 'merge';

describe('Merge Demo', () => {
    it('merge non intersecting maps', () => {
        assert.deepEqual({a: 1, b: 2}, merge({}, {a: 1}, {b: 2}));
    });
    it('merge deep maps', () => {
        assert.deepEqual({a: {a: 1, b: 2}}, merge.recursive({}, {a: {a: 1}}, {a: {b: 2}}));
    });

    it('merge list of maps', () => {
        assert.deepEqual({a: 1, b: 2}, merge.apply(null, [{}, {a:  1}, {b: 2}]));
    });
});
