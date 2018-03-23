import PingPongSetsStrategy from './PingPongSetsStrategy.js';

const rules = {setsToWin: 3};

describe('ping pong sets strategy', () => {
    const pingPongStrategy = new PingPongSetsStrategy();

    it('winnerOptions returns setsToWin',
       () => expect(pingPongStrategy.winnerOptions(rules)).toEqual([3]));
    it('loserOptions returns range from 0 till setsToWin',
       () => expect(pingPongStrategy.loserOptions(rules)).toEqual([0, 1, 2]));
});
