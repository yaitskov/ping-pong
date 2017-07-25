'use strict';

angular.
    module('completeMatch').
    component('completeMatch', {
        templateUrl: 'complete-match/complete-match.template.html',
        controller: ['mainMenu', 'Match', '$routeParams', 'pageCtx', 'requestStatus', '$scope',
                     function (mainMenu, Match, $routeParams, pageCtx, requestStatus, $scope) {
                         mainMenu.setTitle('Match Scoring');
                         this.participants = pageCtx.getMatchParticipants($routeParams.matchId);
                         this.rated = 0;
                         this.possibleScores = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12];
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
                             Match.scoreMatch(
                                 {mid: $routeParams.matchId,
                                  scores: findScores()},
                                 function (okResp) {
                                     requestStatus.complete();
                                     self.rated = 1;
                                     requestStatus.startLoading("Match is scored");
                                     pageCtx.put('last-admin-scoring-tournament-id', $routeParams.matchId);
                                 },
                                 function (error) {
                                     requestStatus.failed(error);
                                 });
                         };
                     }]});
