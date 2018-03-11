import injectTagEdges from './injectTagEdges.js';
import * as ds from 'test/visDataSet.js';

describe('injectTagEdges', () => {
    it('inject tag edge', () => {
        expect(ds.items(injectTagEdges({rootTaggedMatches: [{mid: 1, level: 2, tag: {number: 3}}]})))
            .toEqual([{from: 1, to: 'tag1', arrow: 'to', id: jasmine.any(String)}]);
    });
});
