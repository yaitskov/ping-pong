import angular from 'angular';
import template from './base-score-set.template.html';

angular.
    module('scoreSet').
    component('baseScoreSet', {
        templateUrl: template,
        controller: ['requestStatus', '$scope', '$location',
                     'syncTranslate', '$rootScope', 'binder', '$routeParams',
                     function (requestStatus, $scope, $location,
                               syncTranslate, $rootScope, binder, $routeParams) {
                         var sBtnTrans = syncTranslate.create();
                         var self = this;
                         self.winnerIdx = 0;

                         self.activate = function (idx) {
                             if (self.winnerIdx != idx) {
                                 self.scores.reverse();
                                 self.winnerIdx = idx;
                             }
                         };
                         self.reset = function () {
                             if (self.match.setScores) {
                                 self.scores = self.match.setScores.slice();
                                 var winScore = Math.max.apply(null, self.scores);
                                 var lostScore = Math.min.apply(null, self.scores);
                                 self.possibleWinScores = [];
                                 self.possibleLostScores = [];
                                 self.winnerIdx = (winScore == self.scores[0]) ? 0 : 1;
                                 self.pick(self.winnerIdx, winScore);
                                 self.pick(1 - self.winnerIdx, lostScore);
                                 for (var i = 0; i <= Math.max(self.match.minGamesToWin, lostScore); ++i) {
                                     self.possibleLostScores.push(i);
                                 }
                                 var winLimit = Math.max(self.match.minGamesToWin, winScore);
                                 for (var i = self.match.minGamesToWin; i <= winLimit; ++i) {
                                     self.possibleWinScores.push(i);
                                 }
                             } else {
                                 self.possibleWinScores = [self.match.minGamesToWin];
                                 self.possibleLostScores = [];
                                 self.scores = [-1, -1];
                                 self.pick(self.winnerIdx, self.match.minGamesToWin);
                                 for (var i = 0 ; i <= self.match.minGamesToWin; ++i) {
                                     self.possibleLostScores.push(i);
                                 }
                             }
                         }
                         self.extendWinScore = function () {
                             self.possibleLostScores.length = 0;
                             var last = self.possibleWinScores[self.possibleWinScores.length - 1];
                             self.possibleWinScores.length = 0;
                             for (var i = 0; i < 3; ++i) {
                                 self.possibleWinScores.push(++last);
                             }
                         };
                         self.pick = function (idx, score) {
                             self.scores[idx] = score;
                         };
                         function findScores() {
                             return [{uid: self.participants[self.winnerIdx].uid,
                                      score: self.scores[self.winnerIdx]},
                                     {uid: self.participants[1 - self.winnerIdx].uid,
                                      score: self.scores[1 - self.winnerIdx]}];
                         }
                         self.onMatchSet = function (match) {
                             console.log("caught event.match.set");
                             self.match = match;
                             self.participants = match.participants;
                             self.tournamentId = match.tid;
                             self.nextScoreUpdated();
                             self.reset();
                         };
                         self.nextScoreUpdated = function () {
                             sBtnTrans.trans(['Score Set', {n: 1 + self.match.playedSets}], function (v) {
                                 $rootScope.$broadcast('event.match.set.playedSets', v);
                                 self.setScoreBtn = v;
                             });
                         };
                         binder($scope, {
                             'event.match.set': (event, match) => self.onMatchSet(match),
                             'event.match.set.next': (event, okResp) => {
                                 requestStatus.startLoading(['Set n scored. Match continues', {n: 1 + self.match.playedSets}]);
                                 self.match.playedSets = okResp.nextSetNumberToScore;
                                 self.nextScoreUpdated();
                                 self.reset();
                             },
                             'event.match.score.raise.conflict': (event, data) => {
                                 $rootScope.$broadcast('event.match.score.conflict',
                                                       {matchId: $routeParams.matchId,
                                                        matchScore: resp.data.matchScore,
                                                        yourSetScore: findScores(),
                                                        participants: self.match.participants,
                                                        yourSet: self.match.playedSets});
                             },
                             'event.match.set.score': (event) => {
                                 requestStatus.startLoading("Documenting the score");
                                 if (self.scores[0] < 0 || self.scores[1] < 0) {
                                     requestStatus.validationFailed("Not all participants have been scored");
                                     return;
                                 }
                                 if (self.scores[0] == self.scores[1]) {
                                     requestStatus.validationFailed("Participants cannot have same scores");
                                     return;
                                 }
                                 $rootScope.$broadcast(
                                     'event.match.set.scored',
                                     {mid: $routeParams.matchId,
                                      tid: self.tournamentId,
                                      setOrdNumber: self.match.playedSets,
                                      scores: findScores()});
                             }
                         });
                         $rootScope.$broadcast('event.base.match.set.ready');
                     }]});
