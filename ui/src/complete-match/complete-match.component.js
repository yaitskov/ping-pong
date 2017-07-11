'use strict';

angular.
    module('completeMatch').
    component('completeMatch', {
        templateUrl: 'complete-match/complete-match.template.html',
        controller: ['mainMenu', 'Match', '$routeParams', '$timeout', 'pageCtx',
                     function (mainMenu, Match, $routeParams, $timeout, pageCtx) {
                         mainMenu.setTitle('Match Scoring');
                         this.match = null;
                         this.info = null;
                         this.winnerUid = null;
                         this.participants = pageCtx.getMatchParticipants($routeParams.matchId);
                         this.score = "0";
                         var self = this;
                         self.error = null;
                         function scores (myScore, enemyScore) {
                             return [{uid: self.participants[0].uid, score: myScore},
                                     {uid: self.participants[1].uid, score: enemyScore}];
                         }
                         function findScores() {
                             if (self.winnerUid == self.participants[0].uid) {
                                 return scores(3, self.score | 0);
                             } else {
                                 return scores(self.score | 0, 3);
                             }
                         }
                         this.scoreMatch = function () {
                             self.error = null;
                             if (!self.winnerUid) {
                                 self.error = "Winner is not chosen. Choose it.";
                                 return;
                             }
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
                                     if (error.status == 502) {
                                         self.error = "Server is not available";
                                     } else if (error.status == 400) {
                                         if (error.data.error == 'BadState') {
                                             self.error = "The match is already scored. Redirect back...";
                                             $timeout(function () {
                                                 window.history.back();
                                             }, 2000);
                                         } else {
                                             self.error = "Failed to score";
                                         }
                                     } else {
                                         self.error = "Failed to score.";
                                     }
                                 });
                         };
                     }]});
