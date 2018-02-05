import VisLadder from './VisLadder.js';

describe('VisLadder', () => {
    it('init empty ladder', () => {
        const container = document.createElement('div');
        const ladder = new VisLadder(container, {tid: 1});
    });
});
