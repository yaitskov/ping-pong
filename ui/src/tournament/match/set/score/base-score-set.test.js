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
        mgtw: 11,
        maig: 2,
        countOnlySets: false
    }
};

const PingPongLostDefault = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9];

describe('base-score-set', () => {
    var initEventFired = false;

    const ctx = setupAngularJs(
        'base-score-set',
        {onInit: (scope) => {
            console.log("bind to ready event");
            scope.$on('event.base.match.set.ready', ($event) => {
                console.log("ready event is fired");
                initEventFired = true;
            });
        }});

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

    ij('extend win balls in ping pong match', ($rootScope) => {
        $rootScope.$broadcast('event.match.set', PingPongZeroSets);

        const captured = [];
        ctx.scope.$on('event.base.match.set.pick.lost', (e, data) => captured.push(data));
        ctx.sync();
        ctx.element.find('#extend-win-score').click();

        expect(ctx.ctrl.scores).toEqual([12, 10]);
        expect(ctx.ctrl.possibleWinScores).toEqual([11, 12, 13, 14]);
        expect(ctx.ctrl.possibleLostScores).toEqual([10]);
        expect(captured[0]).toEqual({setOrdNumber: 0,
                                     scores: [{uid: UidA, score: 12},
                                              {uid: UidB, score: 10}]});

        ctx.element.find('#extend-win-score').click();

        expect(ctx.ctrl.scores).toEqual([15, 13]);
        expect(ctx.ctrl.possibleWinScores).toEqual([14, 15, 16, 17]);
        expect(ctx.ctrl.possibleLostScores).toEqual([13]);
        expect(captured[1]).toEqual({setOrdNumber: 0,
                                     scores: [{uid: UidA, score: 15},
                                              {uid: UidB, score: 13}]});
    });

    ij('shrink win balls in ping pong match', ($rootScope) => {
        $rootScope.$broadcast('event.match.set', PingPongZeroSets);

        ctx.ctrl.scores = [15, 13];
        ctx.ctrl.possibleWinScores = [14, 15, 16, 17];
        ctx.ctrl.possibleLostScores = [13];

        ctx.ctrl.pick(ctx.ctrl.winnerIdx, 14);

        expect(ctx.ctrl.possibleWinScores).toEqual([11, 12, 13, 14]);
        expect(ctx.ctrl.possibleLostScores).toEqual([12]);
        expect(ctx.ctrl.scores).toEqual([14, 12]);

        ctx.ctrl.pick(ctx.ctrl.winnerIdx, 11);

        expect(ctx.ctrl.scores).toEqual([11, -1]);
        expect(ctx.ctrl.possibleWinScores).toEqual([11]);
        expect(ctx.ctrl.possibleLostScores).toEqual(PingPongLostDefault);
    });

    describe('pickLost', () => {
        ij('send msg', ($rootScope) => {
            $rootScope.$broadcast('event.match.set', PingPongZeroSets);
            const captured = [];
            ctx.scope.$on('event.base.match.set.pick.lost', (e, data) => captured.push(data));
            ctx.ctrl.scores = [-1, -1];
            ctx.ctrl.pickLost(1, 777);
            expect(captured).toEqual([{setOrdNumber: 0,
                                       scores: [{uid: UidA, score: -1},
                                                {uid: UidB, score: 777}]}]);
        });
    });

    describe('event.match.set.score', () => {
        ij('validation reject due not all participants are scored', (requestStatus, $rootScope) => {
            spyOn(requestStatus, 'validationFailed');
            ctx.ctrl.scores = [-1, -1];
            $rootScope.$broadcast('event.match.set.score');
            expect(requestStatus.validationFailed).toHaveBeenCalledWith("Not all participants have been scored");
        });
        ij('validation reject due not all participants are equally strong', (requestStatus, $rootScope) => {
            spyOn(requestStatus, 'validationFailed');
            ctx.ctrl.scores = [11, 11];
            $rootScope.$broadcast('event.match.set.score');
            expect(requestStatus.validationFailed).toHaveBeenCalledWith("Participants cannot have same scores");
        });

        ij('event.match.set.scored is emitted', ($rootScope) => {
            $rootScope.$broadcast('event.match.set', PingPongZeroSets);

            const captured = [];
            ctx.scope.$on('event.match.set.scored', (e, data) => captured.push(data));
            ctx.ctrl.scores = [11, 0];
            $rootScope.$broadcast('event.match.set.score');
            expect(captured).toEqual([{mid: undefined,
                                       tid: Tid,
                                       setOrdNumber: 0,
                                       scores: [{uid: UidA, score: 11}, {uid: UidB, score: 0}]}]);
        });
    });

    ij('event.match.set.next updates set number', ($rootScope) => {
        $rootScope.$broadcast('event.match.set', PingPongZeroSets);
        expect(ctx.ctrl.match.playedSets).toBe(0);
        $rootScope.$broadcast('event.match.set.next', {nextSetNumberToScore: 2});
        expect(ctx.ctrl.match.playedSets).toBe(2);
    });

    ij('event.match.score.raise.conflict emit and enrich event.match.score.conflict', ($rootScope) => {
        $rootScope.$broadcast('event.match.set', PingPongZeroSets);

        const captured = [];
        ctx.scope.$on('event.match.score.conflict', (e, data) => captured.push(data));

        $rootScope.$broadcast('event.match.score.raise.conflict', {data: {}});
        expect(captured.length).toBe(1);
    });
});
