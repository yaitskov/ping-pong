import angular from 'angular';
import template from './complete-match.template.html';

angular.
    module('completeMatch').
    component('completeMatch', {
        templateUrl: template,
        controller: ['mainMenu', 'Match', '$routeParams', 'pageCtx', 'requestStatus', '$scope',
                     function (mainMenu, Match, $routeParams, pageCtx, requestStatus, $scope) {
                         mainMenu.setTitle('Match Scoring');
                         this.participants = pageCtx.getMatchParticipants($routeParams.matchId);
                         this.tournamentId = pageCtx.get('last-admin-scoring-tournament-id');
                         var maxScore = pageCtx.get('match-max-score-' + $routeParams.matchId);
                         this.possibleScores = [];
                         for (var i =0 ; i <= maxScore; ++i) {
                             this.possibleScores.push(i);
                         }
                         this.scores = [-1, -1];
                         var self = this;
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
                         this.scoreMatch = function () {
                             requestStatus.startLoading("Documenting the score");
                             if (self.scores[0] < 0 || self.scores[1] < 0) {
                                 requestStatus.validationFailed("Not all participants have been scored");
                                 return;
                             }
                             if (self.scores[0] == self.scores[1]) {
                                 requestStatus.validationFailed("Participants cannot have same scores");
                                 return;
                             }
                             if (self.scores[0] < maxScore && self.scores[1] < maxScore) {
                                 requestStatus.validationFailed(["Match continues until", {maxScore: maxScore}]);
                                 return;
                             }
                             Match.scoreMatch(
                                 {mid: $routeParams.matchId,
                                  tid: self.tournamentId,
                                  scores: findScores()},
                                 function (okResp) {
                                     requestStatus.complete();
                                     window.history.back();
                                 },
                                 requestStatus.failed);
                         };
                     }]});
