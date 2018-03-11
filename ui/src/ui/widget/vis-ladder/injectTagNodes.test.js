import injectTagNodes from './injectTagNodes.js';
import * as ds from 'test/visDataSet.js';
import * as mocks from './visLadderMocks.js';

describe('injectTagNodes', () =>
         it('inject tag nodes', () =>
            expect(injectTagNodes({
                matches: [mocks.fullDraftMatch()],
                rootTaggedMatches: [{mid: 1,
                                     level: 2,
                                     tag: {number: 3}}]}).get('tag1')).
            toEqual({id: 'tag1',
                     level: 3,
                     shape: 'box',
                     label: 'Level 3',
                     color: {
                         background: 'orange',
                         border: 'black'
                     }
                    })));
