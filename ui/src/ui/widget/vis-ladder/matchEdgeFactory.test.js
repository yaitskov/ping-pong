import matchEdgeFactory from './matchEdgeFactory.js';
import * as ds from 'test/visDataSet.js';

describe('matchEdgeFactory', () => {
    it('tranistions are undefined',
       () => {
           expect(ds.items(matchEdgeFactory({}))).toEqual([]);
       });
    it('exend transitions with arrow and id',
       () => {
           expect(ds.items(matchEdgeFactory({transitions: [{from: 3}]}))).
               toEqual([{from: 3, id: 1, arrow: 'to'}]);
       });
});
