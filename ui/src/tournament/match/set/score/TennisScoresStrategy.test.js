import TennisScoresStrategy from './TennisScoresStrategy.js';

const rulesSTB2 = {minGamesToWin: 6, minAdvanceInGames: 2,
                   superTieBreakGames: 10, setsToWin: 2};
const rulesSTB3 = Object.assign({}, rulesSTB2, {setsToWin: 3});
const rulesSTB4 = Object.assign({}, rulesSTB2, {setsToWin: 4});

describe('tennis scores strategy', () => {
    const tennisStrategy = new TennisScoresStrategy();

    describe('_isSuperTieBreak', () => {
        it('up to 2 win sets', () => {
            expect(tennisStrategy._isSuperTieBreak(rulesSTB2, 2)).toBeTrue();
            expect(tennisStrategy._isSuperTieBreak(rulesSTB2, 1)).toBeFalse();
        });
        it('up to 3 win sets', () => {
            expect(tennisStrategy._isSuperTieBreak(rulesSTB3, 4)).toBeTrue();
            expect(tennisStrategy._isSuperTieBreak(rulesSTB3, 3)).toBeFalse();
            expect(tennisStrategy._isSuperTieBreak(rulesSTB3, 2)).toBeFalse();
        });
        it('up to 4 win sets', () => {
            expect(tennisStrategy._isSuperTieBreak(rulesSTB4, 6)).toBeTrue();
            expect(tennisStrategy._isSuperTieBreak(rulesSTB4, 5)).toBeFalse();
            expect(tennisStrategy._isSuperTieBreak(rulesSTB4, 4)).toBeFalse();
        });
    });

    describe('winnerOptions', () => {
        it('super tie break', () => {
            expect(tennisStrategy.winnerOptions(rulesSTB2, 'winScore ignored', 2)).toEqual([10]);
        });
        it('no super tie break', () => {
            expect(tennisStrategy.winnerOptions(rulesSTB2, 'winScore ignored', 0)).toEqual([6, 7]);
            expect(tennisStrategy.winnerOptions(rulesSTB2, 'winScore ignored', 1)).toEqual([6, 7]);
        });
    });

    describe('loserOptions', () => {
        it('super tie break', () => expect(tennisStrategy.loserOptions(rulesSTB2, 'winScore ignore', 2)).
           toEqual([0, 1, 2, 3, 4, 5, 6, 7, 8, 9]));

        it('winner got min games to win', () => expect(tennisStrategy.loserOptions(rulesSTB2, 6, 1)).
           toEqual([0, 1, 2, 3, 4]));

        it('winner got min games + 1 to win', () => expect(tennisStrategy.loserOptions(rulesSTB2, 7, 1)).
           toEqual([5, 6]));

        it('winner got to much games', () => expect(() => tennisStrategy.loserOptions(rulesSTB2, 8, 1)).
           toThrow(new RangeError('winner got to much games: 8')));
    });

    describe('defaultWinnerScore', () => {
        it('super tie break', () => expect(tennisStrategy.defaultWinnerScore(rulesSTB2, 2)).toBe(10));
        it('no super tie break', () => expect(tennisStrategy.defaultWinnerScore(rulesSTB2, 1)).toBe(6));
    });
});
