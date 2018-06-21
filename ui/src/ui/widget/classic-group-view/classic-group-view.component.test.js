import { setupAngularJs } from 'test/angularjs-test-setup.js';
import ClassicGroupViewCtrl from './ClassicGroupViewCtrl.js';
import { ruleId } from 'tournament/parameters/group/rules.js';


const matchStates = ['Pending', 'Run', 'Over', 'WalkOver', 'WalkWiner'];
const bidStates = ['Quit', 'Win1', 'Play'];

function generateParticipants(n = 3, prefix = 'Part Icipant') {
    const result = [];
    for (let i = 0; i < n; ++i) {
        const p = {uid: i,
                   name: `${prefix}${i+1}`,
                   state: bidStates[i % bidStates.length],
                   seedPosition: i,
                   finishPosition: n - i,
                   originMatches: {},
                   reasonChain: [
                       {'@type': 'f2f', 'rule': ruleId.f2f, won: i % 2, uid: i, opponentUid: (i + 1) % n},
                       {'@type': 'INF', 'rule': ruleId.DM},
                       {'@type': 'DI', 'rule': ruleId.WS, value: n - i}
                   ]
                  };
        result.push(p);
        for (let j = 0; j < n; ++j) {
            const mid = i + j;
            p.originMatches[mid] = {
                state: matchStates[mid % matchStates.length],
                sets: {his: i, enemy: j},
                games: [{his: i, enemy: j}],
                mid: mid
            };
        }
    }
    return result;
}

describe('classic-group-view', () => {
    const ctx = setupAngularJs('classic-group-view');

    describe('group of 3', () => {
        beforeEach(() => {
            ctx.send(ClassicGroupViewCtrl.TopicLoad,
                     {
                         participants: generateParticipants(3),
                         tid: 1,
                         quitsGroup: 1,
                         sportType: 'PP'
                     });
        });
        describe('number of table rows equals number of participants', () => {
            it('ctx.element is not null',
               () => expect(ctx.element.find('table tbody tr').length).toBe(3));
        });
        describe('number of table columns', () => {
            it('ctx.element is not null',
               () => expect(ctx.element.find('table tbody tr:first td').length).toBe(2 + 3 + 3));
        });
    });
});
