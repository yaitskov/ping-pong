import angular from 'angular';
import template from './base-score-set.template.html';
import possibleScoresStrategies from './possibleScoresStrategy.js';

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
                                 let winScore = Math.max.apply(null, self.scores);
                                 let lostScore = Math.min.apply(null, self.scores);
                                 self.winnerIdx = (winScore == self.scores[0]) ? 0 : 1;
                                 self.scores[self.winnerIdx] = winScore;
                                 self.scores[1 - self.winnerIdx] = lostScore;
                                 const sport = self.match.sport;
                                 self.possibleWinScores = self.scoreStrategy.winnerOptions(
                                     sport, winScore, self.match.playedSets);
                                 self.possibleLostScores = self.scoreStrategy.loserOptions(
                                     sport, winScore, self.match.playedSets);
                             } else {
                                 self.scores = [-1, -1];
                                 self.noBalance();
                             }
                         };
                         self.noBalance = function () {
                             const sport = self.match.sport;
                             const winScore = self.scoreStrategy.defaultWinnerScore(sport, self.match.playedSets);
                             self.scores[self.winnerIdx] = winScore;
                             self.possibleWinScores = self.scoreStrategy.winnerOptions(sport, winScore, self.match.playedSets);
                             self.possibleLostScores = self.scoreStrategy.loserOptions(sport,
                                 self.scores[self.winnerIdx],
                                 self.match.playedSets);
                         };
                         self.extendWinScore = () => {
                             const max = self.possibleWinScores[self.possibleWinScores.length - 1];
                             self.pick(self.winnerIdx, max + 1);
                         };
                         self.isLostToBig = (playerIdx) =>
                             self.possibleLostScores[self.possibleLostScores.length - 1] < self.scores[playerIdx];
                         self.isLostToSmall = (playerIdx) => self.possibleLostScores[0] > self.scores[playerIdx];
                         self.otherPlayer = (playerIdx) => 1 - playerIdx;
                         self.resetPlayerScore = (playerIdx) => {
                             self.scores[playerIdx] = -1;
                         };
                         self.pick = (idx, score) => {
                             self.scores[idx] = score;
                             const sport = self.match.sport;
                             self.possibleWinScores = self.scoreStrategy.winnerOptions(sport, score, self.match.playedSets);
                             self.possibleLostScores = self.scoreStrategy.loserOptions(sport, score, self.match.playedSets);
                             if (self.possibleLostScores.length == 1) {
                                 self.scores[1 - idx] = self.possibleLostScores[0];
                                 $rootScope.$broadcast('event.base.match.set.pick.lost',
                                                       {setOrdNumber: self.match.playedSets,
                                                        scores: findScores()});
                             } else {
                                 const otherPlayerIdx = self.otherPlayer(idx);
                                 if (self.isLostToBig(otherPlayerIdx) || self.isLostToSmall(otherPlayerIdx)) {
                                     self.resetPlayerScore(otherPlayerIdx);
                                 }
                             }
                         };
                         function findScores() {
                             return [{uid: self.participants[self.winnerIdx].uid,
                                      score: self.scores[self.winnerIdx]},
                                     {uid: self.participants[1 - self.winnerIdx].uid,
                                      score: self.scores[1 - self.winnerIdx]}];
                         }
                         self.pickLost = (idx, score) => {
                             self.scores[idx] = score;
                             $rootScope.$broadcast('event.base.match.set.pick.lost',
                                                   {setOrdNumber: self.match.playedSets,
                                                    scores: findScores()});
                         };
                         self.onMatchSet = function (match) {
                             console.log("caught event.match.set");
                             self.match = match;
                             self.scoreStrategy = possibleScoresStrategies[match.sport['@type']];
                             self.participants = match.participants;
                             self.tournamentId = match.tid;
                             self.nextScoreUpdated();
                             self.reset();
                         };
                         self.nextScoreUpdated = function () {
                             sBtnTrans.trans(['Score Set', {n: 1 + self.match.playedSets}], function (v) {
                                 $rootScope.$broadcast('event.match.set.playedSets', v, 1 + self.match.playedSets);
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
                                                        matchScore: data.data.matchScore,
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
