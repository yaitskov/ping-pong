import angular from 'angular';
import template from './admin-score-set.template.html';

angular.
    module('adminScoreSet').
    component('adminScoreSet', {
        templateUrl: template,
        controller: ['mainMenu', 'Match', '$routeParams', 'pageCtx', 'requestStatus', '$scope', '$location', 'syncTranslate',
                     function (mainMenu, Match, $routeParams, pageCtx, requestStatus, $scope, $location, syncTranslate) {
                         mainMenu.setTitle('Match Scoring');
                         var sBtnTrans = syncTranslate.create();
                         this.participants = pageCtx.getMatchParticipants($routeParams.matchId);
                         this.tournamentId = pageCtx.get('last-admin-scoring-tournament-id');
                         this.nextSetNumberToScore = pageCtx.get('next-set-number' + $routeParams.matchId) || 0;
                         var self = this;
                         self.winnerIdx = 0;
                         this.activate = function (idx) {
                             self.scores.reverse();
                             self.winnerIdx = idx;
                         };
                         this.reset = function () {
                             self.possibleWinScores = [self.rules.minGamesToWin];
                             self.possibleLostScores = [];
                             self.scores = [-1, -1];
                             for (var i = 0 ; i <= self.rules.minGamesToWin; ++i) {
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
                         requestStatus.startLoading('Load match rules');
                         Match.getRules({tournamentId: this.tournamentId},
                                        function (rules) {
                                            requestStatus.complete();
                                            self.rules = rules;
                                            self.reset();
                                        },
                                        requestStatus.failed);
                         this.nextScoreUpdated = function () {
                             sBtnTrans.trans(['Score Set', {n: 1 + self.nextSetNumberToScore}], function (v) {
                                 self.setScoreBtn = v;
                             });
                         };
                         self.nextScoreUpdated();
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
                                  setOrdNumber: self.nextSetNumberToScore,
                                  scores: findScores()},
                                 function (okResp) {
                                     requestStatus.complete();
                                     if (okResp.scoreOutcome == 'MatchComplete' || okResp.scoreOutcome == 'LastMatchComplete') {
                                         pageCtx.put('match-score-review-' + $routeParams.matchId, okResp.matchScore);
                                         $location.path('/review/scored-match/' + $routeParams.matchId);
                                     } else if (okResp.scoreOutcome == 'MatchContinues') {
                                         requestStatus.startLoading(['Set n scored. Match continues', {n: 1 + self.nextSetNumberToScore}]);
                                         self.nextSetNumberToScore = okResp.nextSetNumberToScore;
                                         self.nextScoreUpdated();
                                         self.reset();
                                     } else {
                                         requestStatus.validationFailed(["Match score response unknown", {name: okResp.scoreOutcome}]);
                                     }
                                 },
                                 function (resp) {
                                     if (resp.status = 400) {
                                         if (resp.data.error == 'matchScored') {
                                             pageCtx.put('match-score-conflict-' + $routeParams.matchId,
                                                         {'matchScore': resp.data.matchScore,
                                                          'yourSetScore': findScores(),
                                                          'yourSet': self.nextSetNumberToScore});
                                             $location.path('/match/conflict-review/' + $routeParams.matchId);
                                         } else {
                                             requestStatus.failed(resp);
                                         }
                                     } else {
                                         requestStatus.failed(resp);
                                     }
                                 });
                         };
                     }]});
