'use strict';

angular.
    module('completeMyMatch').
    component('completeMyMatch', {
        templateUrl: 'complete-my-match/complete-my-match.template.html',
        controller: ['auth', 'mainMenu', 'Match', '$routeParams',
                     'pageCtx', 'requestStatus', '$scope',
                     function (auth, mainMenu, Match, $routeParams,
                               pageCtx, requestStatus, $scope) {
                         mainMenu.setTitle('Match Scoring');
                         this.match = pageCtx.get('last-scoring-match') || {};
                         var maxScore = pageCtx.get('match-max-score-' + $routeParams.matchId);
                         var self = this;
                         this.possibleScores = [];
                         for (var i =0 ; i <= maxScore; ++i) {
                             this.possibleScores.push(i);
                         }
                         this.scores = [-1, -1];
                         this.pick = function (idx, score) {
                             self.scores[idx] = score;
                         };
                         this.scoreMatch = function () {
                             requestStatus.startLoading('Scoring');
                             if (self.scores[0] < 0 || self.scores[1] < 0) {
                                 requestStatus.validationFailed("Not all participants have been scored");
                                 return;
                             }
                             if (self.scores[0] == self.scores[1]) {
                                 requestStatus.validationFailed("Participants cannot have same scores");
                                 return;
                             }
                             if (self.scores[0] < maxScore && self.scores[1] < maxScore) {
                                 requestStatus.validationFailed("Match continues until " + maxScore);
                                 return;
                             }
                             Match.scoreMatch(
                                 {mid: $routeParams.matchId,
                                  scores: [{uid: auth.myUid(), score: self.scores[0]},
                                           {uid: pageCtx.getEnemyUid(auth.myUid(), $routeParams.matchId),
                                            score: self.scores[1]}]},
                                 function (okResp) {
                                     requestStatus.complete();
                                     window.history.back();
                                 },
                                 requestStatus.failed);
                         };
                     }]});
