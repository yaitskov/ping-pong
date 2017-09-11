import angular from 'angular';
import template from './admin-score-set.template.html';

angular.
    module('adminScoreSet').
    component('adminScoreSet', {
        templateUrl: template,
        controller: ['mainMenu', 'Match', '$routeParams', 'pageCtx', 'requestStatus', '$scope',
                     function (mainMenu, Match, $routeParams, pageCtx, requestStatus, $scope) {
                         mainMenu.setTitle('Match Scoring');
                         this.participants = pageCtx.getMatchParticipants($routeParams.matchId);
                         this.tournamentId = pageCtx.get('last-admin-scoring-tournament-id');
                         this.nextSetNumberToScore = pageCtx.get('next-set-number' + $routeParams.matchId);
                         var self = this;
                         var minScoreToWin = pageCtx.get('min-set-score-' + $routeParams.matchId);
                         self.winnerIdx = 0;
                         this.reset = function () {
                             self.possibleWinScores = [minScoreToWin];
                             self.possibleLostScore = [];
                             self.scores = [-1, -1];
                         }
                         this.extendWinScore = function () {
                             self.possibleLostScore.length = 0;
                             var last = self.possibleWinScores[self.possibleWinScores.length - 1];
                             self.possibleWinScores.length = 0;
                             for (int i = 0; i < 3; ++i) {
                                 self.possibleWinScores.push(++last);
                             }
                         };
                         for (var i = 0 ; i <= minScoreToWin; ++i) {
                             self.possibleLostScores.push(i);
                         }
                         this.pick = function (idx, score) {
                             self.scores[idx] = score;
                         };
                         function findScores() {
                             var result = [];
                             for (var i in self.participants) {
                                 result.push({uid: self.participants[i].uid,
                                              score: self.scores[i]});
                             }
                             return result;
                         }
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
                                  ordOrdNumber: self.nextSetNumberToScore,
                                  scores: findScores()},
                                 function (okResp) {
                                     requestStatus.complete();
                                     if (okResp.scoreOutcome == 'MatchComplete' || okResp.scoreOutcome == 'LastMatchComplete') {
                                         pageCtx.put('match-score-review-' + $routeParams.matchId, okResp.matchScore);
                                         $location.path('/review/scored-match/' + $routeParams.matchId);
                                     } else if (okResp.scoreOutcome == 'MatchContinues') {
                                         requestStatus.startLoading(['Set n scored. Match continues', {n: self.nextSetNumberToScore}]);
                                         self.reset();
                                         self.nextSetNumberToScore = okResp.nextSetNumberToScore;
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
