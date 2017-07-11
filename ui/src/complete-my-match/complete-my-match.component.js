'use strict';

angular.
    module('completeMyMatch').
    component('completeMyMatch', {
        templateUrl: 'complete-my-match/complete-my-match.template.html',
        controller: ['auth', 'mainMenu', 'Match', '$routeParams', '$timeout', 'pageCtx',
                     function (auth, mainMenu, Match, $routeParams, $timeout, pageCtx) {
                         mainMenu.setTitle('Match Scoring');
                         this.match = null;
                         this.info = null;
                         this.outcome = "win";
                         this.score = "0";
                         var self = this;
                         self.error = null;
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
                         this.scoreMatch = function () {
                             Match.scoreMyMatch(
                                 {mid: $routeParams.matchId,
                                  scores: findScores()},
                                 function (okResp) {
                                     console.log("scored : " + okResp.data);
                                     self.info = "Result is accepted. Redirect back...";
                                     $timeout(function () {
                                         window.history.back();
                                     }, 2000);
                                 },
                                 function (error) {
                                     self.error = "" + error.status;
                                 });
                         };
                     }]});
