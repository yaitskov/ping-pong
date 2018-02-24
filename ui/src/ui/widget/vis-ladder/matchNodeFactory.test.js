import matchNodeFactory from './matchNodeFactory.js';

describe('matchNodeFactory', () => {
    it('transform match to vis node',
       () => {
           expect(matchNodeFactory({1: 'Bill Gates'},
                                   {id: 2,
                                    level: 3,
                                    state: 'Draft',
                                    score: {1: undefined}})).
               toEqual({id: 2, level: 3, shape: 'box',
                        label: ' ⌛ - Bill Gates\n ⌛ - ???'});
       });
});
