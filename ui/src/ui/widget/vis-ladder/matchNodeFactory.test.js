import matchNodeFactory from './matchNodeFactory.js';
import * as ds from 'test/visDataSet.js';

describe('matchNodeFactory', () => {
    it('transform match to vis node',
       () => {
           expect(ds.items(matchNodeFactory({participants: {1: 'Bill Gates'},
                                    matches: [{id: 2,
                                    level: 3,
                                    state: 'Draft',
                                               score: {1: undefined}}]}))).
               toEqual([{id: 2, level: 3, shape: 'box',
                        label: ' ⌛ - Bill Gates\n ⌛ - ???'}]);
       });
});
