import angular from 'angular';
import template from './match-score-conflict.template.html';

angular.
    module('matchScoreConflict').
    component('matchScoreConflict', {
        templateUrl: template,
        controller: ['mainMenu', 'pageCtx', '$routeParams', 'Match', 'requestStatus',
                     function (mainMenu, pageCtx, $routeParams, Match, requestStatus) {
                         var self = this;
                         mainMenu.setTitle('Match scoring conflict');
                         self.conflict = pageCtx.get('match-score-conflict-' + $routeParams.matchId);
                         self.matchScore = self.conflict.matchScore;
                         self.yourSet = self.conflict.yourSet;
                         self.yourSetScore = self.conflict.yourSetScore;
                         self.participants = pageCtx.getMatchParticipants($routeParams.matchId);
                         var result = [];
                         var l = self.matchScore.sets[self.participants[0].uid].length;
                         for (var i = 0; i < l; ++i) {
                             result.push({a: self.matchScore.sets[self.participants[0].uid][i],
                                          b: self.matchScore.sets[self.participants[1].uid][i]});
                         }
                         this.sets = result;
                         this.isWon = function (set) {
                             return set.a > set.b;
                         };
                         this.rescore = function () {
                             requestStatus.startLoading('Reset match score');
                             Match.resetSetScoreDownTo(
                                 {mid: self.matchScore.mid,
                                  tid: self.matchScore.tid,
                                  setNumber: self.yourSet},
                                 function (ok) {
                                     requestStatus.complete();
                                     requestStatus.startLoading('Setting your set score');
                                     Match.scoreMatch(
                                         {mid: self.matchScore.mid,
                                          tid: self.matchScore.tid,
                                          setOrdNumber: self.yourSet,
                                          scores: self.conflict.yourSetScore},
                                         function (ok) {
                                             requestStatus.complete();
                                             window.history.back();
                                         },
                                         function (resp) {
                                             if (resp.status = 400) {
                                                 if (resp.data.error == 'matchScored') {
                                                     pageCtx.put('match-score-conflict-' + $routeParams.matchId,
                                                                 {'matchScore': resp.data.matchScore,
                                                                  'yourSetScore': self.conflict.yourSetScore,
                                                                  'yourSet': self.yourSet});
                                                     $location.path('/match/conflict-review/' + $routeParams.matchId);
                                                 } else {
                                                     requestStatus.failed(resp);
                                                 }
                                             } else {
                                                 requestStatus.failed(resp);
                                             }
                                         });
                                 },
                                 requestStatus.failed);
                         }
                     }]});