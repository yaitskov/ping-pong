'use strict';

angular.
    module('completeMyMatch').
    component('completeMyMatch', {
        templateUrl: 'complete-my-match/complete-my-match.template.html',
        controller: ['auth', 'mainMenu', 'Match', '$routeParams', '$interval',
                     'pageCtx', 'requestStatus', '$scope',
                     function (auth, mainMenu, Match, $routeParams, $interval,
                               pageCtx, requestStatus, $scope) {
                         mainMenu.setTitle('Match Scoring');
                         this.match = null;
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
                         this.scoreMatch = function () {
                             requestStatus.startLoading('Scoring');
                             Match.scoreMatch(
                                 {mid: $routeParams.matchId,
                                  scores: findScores()},
                                 function (okResp) {
                                     requestStatus.complete();
                                     var ctx = {ticked: 0};
                                     requestStatus.startLoading(
                                         "Result is accepted. Redirect back in 30 seconds");
                                     ctx.timer = $interval(function (counter) {
                                         if (counter > 30) {
                                             $interval.cancel(ctx.timer);
                                             window.history.back();
                                         } else {
                                             requestStatus.startLoading(
                                                 "Result is accepted. Redirect back in "
                                                    + (30 - counter) + " seconds");
                                         }
                                     }, 1000, 0);
                                     $scope.$on('$destroy', function() {
                                         $interval.cancel(ctx.timer);
                                     });
                                 },
                                 requestStatus.failed);
                         };
                     }]});
