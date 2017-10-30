import angular from 'angular';
import template from './score-set.template.html';

angular.
    module('scoreSet').
    component('scoreSet', {
        templateUrl: template,
        controller: ['Match', 'pageCtx', 'requestStatus', '$scope', '$location', 'syncTranslate', '$rootScope', 'binder', '$routeParams',
                     function (Match, pageCtx, requestStatus, $scope, $location, syncTranslate, $rootScope, binder, $routeParams) {
                         var sBtnTrans = syncTranslate.create();
                         var self = this;
                         console.log("bind event.match.set");
                         binder($scope, {
                             'event.match.set': (event, match) => self.onMatchSet(match)
                         });
                         self.winnerIdx = 0;

                         this.activate = function (idx) {
                             if (self.winnerIdx != idx) {
                                 self.scores.reverse();
                                 self.winnerIdx = idx;
                             }
                         };
                         this.reset = function () {
                             self.possibleWinScores = [self.match.minGamesToWin];
                             self.possibleLostScores = [];
                             self.scores = [-1, -1];
                             self.pick(self.winnerIdx, self.match.minGamesToWin);
                             for (var i = 0 ; i <= self.match.minGamesToWin; ++i) {
                                 self.possibleLostScores.push(i);
                             }
                         }
                         this.extendWinScore = function () {
                             self.possibleLostScores.length = 0;
                             var last = self.possibleWinScores[self.possibleWinScores.length - 1];
                             self.possibleWinScores.length = 0;
                             for (var i = 0; i < 3; ++i) {
                                 self.possibleWinScores.push(++last);
                             }
                         };
                         this.pick = function (idx, score) {
                             self.scores[idx] = score;
                         };
                         function findScores() {
                             return [{uid: self.participants[self.winnerIdx].uid,
                                      score: self.scores[self.winnerIdx]},
                                     {uid: self.participants[1 - self.winnerIdx].uid,
                                      score: self.scores[1 - self.winnerIdx]}];
                         }
                         this.onMatchSet = function (match) {
                             console.log("caught event.match.set");
                             self.match = match;
                             self.participants = match.participants;
                             self.tournamentId = match.tid;
                             self.nextScoreUpdated();
                             self.reset();
                         };
                         this.nextScoreUpdated = function () {
                             sBtnTrans.trans(['Score Set', {n: 1 + self.match.playedSets}], function (v) {
                                 self.setScoreBtn = v;
                             });
                         };
                         this.scoreMatchSet = function () {
                             requestStatus.startLoading("Documenting the score");
                             if (self.scores[0] < 0 || self.scores[1] < 0) {
                                 requestStatus.validationFailed("Not all participants have been scored");
                                 return;
                             }
                             if (self.scores[0] == self.scores[1]) {
                                 requestStatus.validationFailed("Participants cannot have same scores");
                                 return;
                             }
                             Match.scoreMatch(
                                 {mid: $routeParams.matchId,
                                  tid: self.tournamentId,
                                  setOrdNumber: self.match.playedSets,
                                  scores: findScores()},
                                 function (okResp) {
                                     requestStatus.complete();
                                     if (okResp.scoreOutcome == 'MatchComplete' || okResp.scoreOutcome == 'LastMatchComplete') {
                                         $rootScope.$broadcast('event.match.scored', okResp.matchScore);
                                     } else if (okResp.scoreOutcome == 'MatchContinues') {
                                         requestStatus.startLoading(['Set n scored. Match continues', {n: 1 + self.match.playedSets}]);
                                         self.matc.playedSets = okResp.nextSetNumberToScore;
                                         self.nextScoreUpdated();
                                         self.reset();
                                     } else {
                                         requestStatus.validationFailed(["Match score response unknown", {name: okResp.scoreOutcome}]);
                                     }
                                 },
                                 function (resp) {
                                     if (resp.status == 400) {
                                         if (resp.data.error == 'matchScored') {
                                             requestStatus.complete();
                                             $rootScope.$broadcast('event.match.score.conflict',
                                                                   {matchId: $routeParams.matchId,
                                                                    matchScore: resp.data.matchScore,
                                                                    yourSetScore: findScores(),
                                                                    participants: self.match.participants,
                                                                    yourSet: self.match.playedSets});
                                         } else {
                                             requestStatus.failed(resp);
                                         }
                                     } else {
                                         requestStatus.failed(resp);
                                     }
                                 });
                         };
                         $rootScope.$broadcast('event.match.set.ready');
                     }]});
