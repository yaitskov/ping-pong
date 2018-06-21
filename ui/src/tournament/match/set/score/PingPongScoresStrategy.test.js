import PingPongScoresStrategy from './PingPongScoresStrategy.js';

const rules = {mgtw: 11, maig: 2};

describe('ping pong scores strategy', () => {
    const pingPongStrategy = new PingPongScoresStrategy();

    describe('winnerOptions', () => {
        it('advance less 14', () => {
            expect(pingPongStrategy.winnerOptions(rules, 12, 0)).toEqual([11, 12, 13, 14]);
            expect(pingPongStrategy.winnerOptions(rules, 14, 0)).toEqual([11, 12, 13, 14]);
        });
        it('advance less 17', () => {
            expect(pingPongStrategy.winnerOptions(rules, 15, 0)).toEqual([14, 15, 16, 17]);
            expect(pingPongStrategy.winnerOptions(rules, 17, 0)).toEqual([14, 15, 16, 17]);
        });
        it('no advance', () => {
            expect(pingPongStrategy.winnerOptions(rules, 11, 0)).toEqual([11]);
        });
        it('exception too small', () => {
            expect(() => pingPongStrategy.winnerOptions(rules, 10, 0)).
                toThrow(new RangeError('win score 10 is too small'));
        });
    });

    describe('loserOptions', () => {
        it('no advance', () => expect(pingPongStrategy.loserOptions(rules, 11)).
           toEqual([0, 1, 2, 3, 4, 5, 6, 7, 8, 9]));

        it('advance', () => expect(pingPongStrategy.loserOptions(rules, 13)).
           toEqual([11]));
    });

    describe('defaultWinnerScore',
             () => it('eq min',
                      () => expect(pingPongStrategy.defaultWinnerScore(rules)).toBe(11)));
});
