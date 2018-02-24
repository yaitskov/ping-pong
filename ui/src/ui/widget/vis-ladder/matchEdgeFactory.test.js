import matchEdgeFactory from './matchEdgeFactory.js';

describe('matchEdgeFactory', () => {
    function items(ds) {
        return ds.get(ds.getIds());
    }
    it('tranistions are undefined',
       () => {
           const ds = matchEdgeFactory({});
           expect(items(ds)).toEqual([]);
       });
    it('exend transitions with arrow and id',
       () => {
           const ds = matchEdgeFactory({transitions: [{from: 3}]});
           expect(items(ds)).toEqual([{from: 3, id: 1, arrow: 'to'}]);
       });
});
