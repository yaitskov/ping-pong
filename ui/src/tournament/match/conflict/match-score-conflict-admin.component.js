import angular from 'angular';
import '../admin-score.scss';
import template from './match-score-conflict-admin.template.html';

angular.
    module('tournament').
    component('matchScoreConflictAdmin', {
        templateUrl: template,
        controller: ['mainMenu', 'pageCtx', '$routeParams', 'Match', 'requestStatus', '$location', 'binder', '$scope',
                     function (mainMenu, pageCtx, $routeParams, Match, requestStatus, $location, binder, $scope) {
                         var self = this;
                         binder($scope, {
                             'event.main.menu.ready': (e) => mainMenu.setTitle('Match scoring conflict')});
                         self.conflict = pageCtx.get('match-score-conflict-' + $routeParams.matchId);
                         self.matchScore = self.conflict.matchScore;
                         self.matchId = $routeParams.matchId;
                         self.tournamentId = $routeParams.tournamentId;

                         self.showReview = function () {
                             pageCtx.put('match-score-review-' + $routeParams.matchId,
                                         {score: self.matchScore,
                                          participants: self.conflict.participants
                                         });
                             $location.path('/review/admin-scored-match/' + self.tournamentId + '/' + self.matchId);
                         };

                         self.continueJudgeMatch = function () {
                             var match = pageCtx.get('last-scoring-match');
                             for (var k in self.matchScore.sets) {
                                 match.playedSets = self.matchScore.sets[k].length;
                                 break;
                             }
                             pageCtx.put('last-scoring-match', match);
                             $location.path('/judge/score/set/' + self.tournamentId + '/' + self.matchId);
                         }

                         self.yourSet = self.conflict.yourSet;
                         self.yourSetScore = self.conflict.yourSetScore;

                         self.rescore = function () {
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
                                             if (ok.matchScore) {
                                                 var match = pageCtx.get('last-scoring-match');
                                                 for (var k in self.matchScore.sets) {
                                                     match.playedSets = self.matchScore.sets[k].length;
                                                     break;
                                                 }
                                                 pageCtx.put('last-scoring-match', match);
                                                 $location.path('/judge/score/set/' + self.tournamentId + '/' + self.matchId);
                                             } else {
                                                 $location.path('/review/admin-scored-match/' + self.tournamentId + '/' + self.matchId);
                                             }
                                         },
                                         function (resp) {
                                             if (resp.status = 400) {
                                                 if (resp.data.error == 'matchScored') {
                                                     pageCtx.put('match-score-conflict-' + $routeParams.matchId,
                                                                 {matchScore: resp.data.matchScore,
                                                                  yourSetScore: self.conflict.yourSetScore,
                                                                  participants: self.conflict.participants,
                                                                  yourSet: self.yourSet});
                                                     $location.path('/match/admin-conflict-review/' + self.tournamentId + '/' + $routeParams.matchId);
                                                 } else {
                                                     requestStatus.failed(resp);
                                                 }
                                             } else {
                                                 requestStatus.failed(resp);
                                             }
                                         });
                                 },
                               (...a) => requestStatus.failed(...a));
                         }
                     }]});
