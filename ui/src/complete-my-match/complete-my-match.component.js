'use strict';

angular.
    module('completeMyMatch').
    component('completeMyMatch', {
        templateUrl: 'complete-my-match/complete-my-match.template.html',
        controller: ['auth', 'mainMenu', 'Match', '$routeParams',
                     'pageCtx', 'requestStatus', '$scope', 'countDown',
                     function (auth, mainMenu, Match, $routeParams,
                               pageCtx, requestStatus, $scope, countDown) {
                         mainMenu.setTitle('Match Scoring');
                         this.match = pageCtx.get('last-scoring-match') || {};
                         this.rated = null;
                         this.outcome = "win";
                         this.score = "0";
                         var self = this;
                         function scores (myScore, enemyScore) {
                             return [{uid: auth.myUid(), score: myScore},
                                     {uid: pageCtx.getEnemyUid(auth.myUid(), $routeParams.matchId),
                                      score: enemyScore}];
                         }
                         function findScores() {
                             if (self.outcome == 'win') {
                                 return scores(3, self.score | 0);
                             } else {
                                 return scores(self.score | 0, 3);
                             }
                         }
                         this.showFinalMessage = function () {
                             countDown.seconds(
                                 $scope, 30,
                                 function (left) {
                                     requestStatus.startLoading(
                                         "The match is scored and complete. Auto redirect back in "
                                             + left + " seconds");
                                 },
                                 function () {
                                     window.history.back();
                                 });
                         };
                         this.scoreMatch = function () {
                             requestStatus.startLoading('Scoring');
                             Match.scoreMatch(
                                 {mid: $routeParams.matchId,
                                  scores: findScores()},
                                 function (okResp) {
                                     self.rated = 1;
                                     requestStatus.complete();
                                     self.showFinalMessage();
                                 },
                                 requestStatus.failed);
                         };
                     }]});
