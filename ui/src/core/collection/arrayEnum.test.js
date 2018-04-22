import arrayEnum from './arrayEnum.js';

describe('arrayEnum', () => {
    it('empty array', () => expect(arrayEnum()).toEqual({}));
    it('1 item', () => expect(arrayEnum('a')).toEqual({a: 'a'}));
    it('repeat item', () => expect(arrayEnum('a', 'b', 'a')).toEqual({a: 'a', b: 'b'}));
});
