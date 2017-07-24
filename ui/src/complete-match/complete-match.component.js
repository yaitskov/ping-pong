'use strict';

angular.
    module('completeMatch').
    component('completeMatch', {
        templateUrl: 'complete-match/complete-match.template.html',
        controller: ['mainMenu', 'Match', '$routeParams', 'pageCtx', 'requestStatus', '$scope',
                     function (mainMenu, Match, $routeParams, pageCtx, requestStatus, $scope) {
                         mainMenu.setTitle('Match Scoring');
                         this.participants = pageCtx.getMatchParticipants($routeParams.matchId);
                         this.rated = 1;
                         this.scores = [];
                         var self = this;
                         function findScores() {
                             var result = [];
                             for (var i in self.participants) {
                                 result.append({uid: self.participants[i].uid,
                                                score: self.scores[i]});
                             }
                             return result;
                         }
                         this.scoreMatch = function () {
                             requestStatus.startLoading();
                             if (!self.scores.length != 2) {
                                 requestStatus.validationFailed("Not all participants have been score.");
                                 return;
                             }
                             Match.scoreMyMatch(
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
