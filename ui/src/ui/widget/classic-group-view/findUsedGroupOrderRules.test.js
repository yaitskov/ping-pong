import findUsedGroupOrderRules from './findUsedGroupOrderRules.js';

describe('findUsedGroupOrderRules', () => {
    it('skip 1 row INF collumn ', () => expect(
        findUsedGroupOrderRules([[{'@type': 'INF'},{'@type': 'INF'}]])).toEqual([]));
    it('skip 2 row INF collumn ', () => expect(
        findUsedGroupOrderRules([[{'@type': 'INF'}],[{'@type': 'INF'}]])).toEqual([]));
    it('INF merge 2 row, first row longer', () => expect(
        findUsedGroupOrderRules([
            [{'@type': 'INF'},{'@type': 'DI', rule: 'WS'}],
            [{'@type': 'INF'}]])).toEqual(['WS']));

    it('INF merge 2 row INF collumn ', () => expect(
        findUsedGroupOrderRules([
            [{'@type': 'INF'}],
            [{'@type': 'INF'},{'@type': 'DI', rule: 'WS'}]])).toEqual(['WS']));

    it('chase bug', () => expect(
        findUsedGroupOrderRules([
            [{'@type': 'f2f', rule: 'f2f'},
             {'@type': 'INF', rule: 'DM'},
             {'@type': 'DI', rule: 'WS'}],
            [{'@type': 'f2f', rule: 'f2f'},
             {'@type': 'INF', rule: 'DM'},
             {'@type': 'DI', rule: 'WS'}],
            [{'@type': 'f2f', rule: 'f2f'},
             {'@type': 'INF', rule: 'DM'},
             {'@type': 'DI', rule: 'WS'}]])).toEqual(['f2f', 'WS']));

});
