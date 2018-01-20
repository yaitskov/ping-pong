import { setupAngularJs, ij } from 'test/angularjs-test-setup.js';
import possibleScoresStrategies from './possibleScoresStrategy.js';

const UidA = 1;
const UidB = 2;
const Tid = 3;

const ParticipantA = {
    uid: UidA,
    name: 'p1'
};

const ParticipantB = {
    uid: UidB,
    name: 'p2'
};

const PingPongZeroSets = {
    tid: Tid,
    playedSets: 0,
    participants: [ParticipantA, ParticipantB],
    sport: {
        '@type': 'PingPong',
        minGamesToWin: 11,
        minAdvanceInGames: 2
    }
};

const PingPongLostDefault = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9];

describe('base-score-set', () => {
    var initEventFired = false;

    const ctx = setupAngularJs('base-score-set', (scope) => {
        console.log("bind to ready event");
        scope.$on('event.base.match.set.ready', ($event) => {
            console.log("ready event is fired");
            initEventFired = true;
        });
    });

    it('ready event is emitted', () => {
        expect(initEventFired).toBeTrue();
    });

    function checkExtendWinScoreVisible(ctx) {
        expect(ctx.element.find('#extend-win-score').hasClass("ng-hide")).toBeFalse();
    }

    ij('set event is handled for new ping pong match', ($rootScope) => {
        $rootScope.$broadcast('event.match.set', PingPongZeroSets);
        ctx.sync();

        expect(ctx.ctrl.match).toBe(PingPongZeroSets);
        expect(ctx.ctrl.tournamentId).toBe(PingPongZeroSets.tid);
        expect(ctx.ctrl.scoreStrategy.showExtend()).toBeTrue();
        expect(ctx.ctrl.scores).toEqual([11, -1]);
        expect(ctx.ctrl.winnerIdx).toBe(0);
        expect(ctx.ctrl.possibleWinScores).toEqual([11]);
        expect(ctx.ctrl.possibleLostScores).toEqual(PingPongLostDefault);

        checkExtendWinScoreVisible(ctx);
    });

    ij('set event is handled for scored ping pong set', ($rootScope) => {
        $rootScope.$broadcast('event.match.set',
                              Object.assign(
                                  {setScores: [3, 11]},
                                  PingPongZeroSets));
        ctx.sync();

        expect(ctx.ctrl.scores).toEqual([3, 11]);
        expect(ctx.ctrl.winnerIdx).toBe(1);
        expect(ctx.ctrl.possibleWinScores).toEqual([11]);
        expect(ctx.ctrl.possibleLostScores).toEqual(PingPongLostDefault);

        checkExtendWinScoreVisible(ctx);
    });

    ij('set event is handled for scored ping pong advance set', ($rootScope) => {
        $rootScope.$broadcast('event.match.set',
                              Object.assign(
                                  {setScores: [10, 12]},
                                  PingPongZeroSets));
        ctx.sync();

        expect(ctx.ctrl.scores).toEqual([10, 12]);
        expect(ctx.ctrl.winnerIdx).toBe(1);
        expect(ctx.ctrl.possibleWinScores).toEqual([11, 12, 13, 14]);
        expect(ctx.ctrl.possibleLostScores).toEqual([10]);

        checkExtendWinScoreVisible(ctx);
    });
});
