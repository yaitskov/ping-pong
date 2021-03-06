import TennisScoresStrategy from './TennisScoresStrategy.js';

const rulesSTB1 = {mgtw: 6, maig: 2,
                   stbg: 10, stw: 1};
const rulesSTB2 = {mgtw: 6, maig: 2,
                   stbg: 10, stw: 2};
const rulesSTB3 = Object.assign({}, rulesSTB2, {stw: 3});
const rulesSTB4 = Object.assign({}, rulesSTB2, {stw: 4});

describe('tennis scores strategy', () => {
    const tennisStrategy = new TennisScoresStrategy();

    describe('_isSuperTieBreak', () => {
        it('up to 1 win sets', () => {
            expect(tennisStrategy._isSuperTieBreak(rulesSTB1, 0)).toBeFalse();
        });
        it('up to 2 win sets', () => {
            expect(tennisStrategy._isSuperTieBreak(rulesSTB2, 2)).toBeTrue();
            expect(tennisStrategy._isSuperTieBreak(rulesSTB2, 1)).toBeFalse();
            expect(tennisStrategy._isSuperTieBreak(rulesSTB1, 0)).toBeFalse();
        });
        it('up to 3 win sets', () => {
            expect(tennisStrategy._isSuperTieBreak(rulesSTB3, 4)).toBeTrue();
            expect(tennisStrategy._isSuperTieBreak(rulesSTB3, 3)).toBeFalse();
            expect(tennisStrategy._isSuperTieBreak(rulesSTB3, 2)).toBeFalse();
            expect(tennisStrategy._isSuperTieBreak(rulesSTB1, 0)).toBeFalse();
        });
        it('up to 4 win sets', () => {
            expect(tennisStrategy._isSuperTieBreak(rulesSTB4, 6)).toBeTrue();
            expect(tennisStrategy._isSuperTieBreak(rulesSTB4, 5)).toBeFalse();
            expect(tennisStrategy._isSuperTieBreak(rulesSTB4, 4)).toBeFalse();
            expect(tennisStrategy._isSuperTieBreak(rulesSTB1, 0)).toBeFalse();
        });
    });

    describe('winnerOptions', () => {
        it('super tie break', () => {
            expect(tennisStrategy.winnerOptions(rulesSTB2, 10, 2)).toEqual([10]);
        });
        it('no super tie break', () => {
            expect(tennisStrategy.winnerOptions(rulesSTB2, 6, 0)).toEqual([6, 7]);
            expect(tennisStrategy.winnerOptions(rulesSTB2, 6, 1)).toEqual([6, 7]);
        });
    });

    describe('loserOptions', () => {
        it('super tie break',
           () => expect(tennisStrategy.loserOptions(rulesSTB2, 10, 2)).
           toEqual([0, 1, 2, 3, 4, 5, 6, 7, 8]));

        it('winner got min games to win',
           () => expect(tennisStrategy.loserOptions(rulesSTB2, 6, 1)).
           toEqual([0, 1, 2, 3, 4]));

        it('winner got min games + 1 to win',
           () => expect(tennisStrategy.loserOptions(rulesSTB2, 7, 1)).
           toEqual([5, 6]));

        it('winner got to much games',
           () => expect(() => tennisStrategy.loserOptions(rulesSTB2, 8, 1)).
           toThrow(new RangeError('winner got to much games: 8')));
    });

    describe('defaultWinnerScore', () => {
        it('super tie break', () => expect(tennisStrategy.defaultWinnerScore(rulesSTB2, 2)).toBe(10));
        it('no super tie break', () => expect(tennisStrategy.defaultWinnerScore(rulesSTB2, 1)).toBe(6));
    });
});
