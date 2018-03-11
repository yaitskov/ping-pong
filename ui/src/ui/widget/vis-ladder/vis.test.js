import vis from 'vis';

describe('vis', () =>
    describe('DataSet', () =>
        it('get by string id', () => {
            const ds = new vis.DataSet([{id: 'tag1', val: 123}]);
            expect(ds.get('tag1')).toEqual({id: 'tag1', val: 123});
        })));
