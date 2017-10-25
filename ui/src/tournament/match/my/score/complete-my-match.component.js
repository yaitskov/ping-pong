import angular from 'angular';
import template from './complete-my-match.template.html';

angular.
    module('tournament').
    component('completeMyMatch', {
        templateUrl: template,
        controller: ['auth', 'mainMenu', 'Match', '$routeParams',
                     'pageCtx', 'requestStatus', '$scope',
                     function (auth, mainMenu, Match, $routeParams,
                               pageCtx, requestStatus, $scope) {
                         mainMenu.setTitle('Match Scoring');
                         this.match = pageCtx.get('last-scoring-match');
                         var maxScore = this.match.matchScore;
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
                                 requestStatus.validationFailed(["Match continues until", {maxScore: maxScore}]);
                                 return;
                             }
                             Match.scoreMatch(
                                 {mid: $routeParams.matchId,
                                  tid: self.match.tid,
                                  scores: [{uid: auth.myUid(), score: self.scores[0]},
                                           {uid: self.match.enemy.uid,
                                            score: self.scores[1]}]},
                                 function (okResp) {
                                     requestStatus.complete();
                                     window.history.back();
                                 },
                                 requestStatus.failed);
                         };
                     }]});
