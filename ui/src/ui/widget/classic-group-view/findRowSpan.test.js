import findRowSpan from './findRowSpan.js';

function sut(reasonChainList) {
    return findRowSpan(reasonChainList, (a) => a < 0, (a, b) => a == b);
}

describe('findRowSpan', () => {
    it('1 x 1', () => expect(sut([[1]])).toEqual({0: {0: 1}}));
    it('3 x 3 no merged cells',
       () => expect(sut([[1, 2, 3], [3, 1, 2], [1, 2, 3]])).toEqual(
           {0: {0: 1, 1: 1, 2: 1}, 1: {0: 1, 1: 1, 2: 1}, 2: {0: 1, 1: 1, 2: 1}}));

    it('2 x 2 merge first column',
       () => expect(sut([[1, 2], [1, 3]])).toEqual(
           {0: {0: 2, 1: 1}, 1: {1: 1}}));

    it('3 x 2 merge fist 2 cells in first column',
       () => expect(sut([[1, 2], [1, 3], [2, 4]])).toEqual(
           {0: {0: 2, 1: 1}, 1: {1: 1}, 2: {0: 1, 1: 1}}));

    it('3 x 2 merge last 2 cells in first column',
       () => expect(sut([[2, 4], [1, 2], [1, 3]])).toEqual(
           {0: {0: 1, 1: 1}, 1: {0: 2, 1: 1}, 2: {1: 1}}));

    it('4 x 2 t merge',
       () => expect(sut([[1], [2, 3], [4, 3], [1]])).toEqual(
           {0: {0: 1}, 1: {0: 1, 1: 2}, 2: {0: 1}, 3: {0: 1}}));

    it('3 x 3 void',
       () => expect(sut([[-1, -2, 1], [-1, -2, 2], [-1, -2, 3]])).toEqual(
           {0: {2: 1}, 1: {2: 1}, 2: {2: 1}}));
});
