import mapO from './mapO.js';

describe('mapO', () => {
    it('return fallback for empty list',
       () => expect(mapO([], () => 1, ['fallback'])).toEqual(['fallback']));
    it('return fallback for false like',
       () => expect(mapO(null, () => 1, ['fallback'])).toEqual(['fallback']));
    it('apply mapper for non empty list',
       () => expect(mapO([1], (x) => x * 2, [0])).toEqual([2]));
});
