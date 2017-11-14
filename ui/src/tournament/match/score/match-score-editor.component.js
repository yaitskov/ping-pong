import 'angular';
import template from './match-score-editor.template.html';

angular.
    module('tournament').
    component('matchScoreEditor', {
        templateUrl: template,
        controller: ['Match', 'mainMenu', '$routeParams',
                     'binder', '$rootScope', '$scope', 'requestStatus', '$location',
                     function (Match, mainMenu, $routeParams,
                               binder, $rootScope, $scope, requestStatus, $location) {
                         var self = this;
                         self.tournamentId = $routeParams.tournamentId;
                         self.matchId = $routeParams.matchId;
                         self.setRescoring = false;
                         self.match = null;
                         self.rescoreMatch = function () {
                             requestStatus.startLoading();
                             Match.rescoreMatch(
                                 {tid: self.match.tid,
                                  mid: self.match.score.mid,
                                  effectHash: self.effectHash,
                                  sets: self.match.score.sets},
                                 (ok) => {
                                     requestStatus.complete();
                                     $location.path('/match/management/' +
                                                    $routeParams.tournamentId + '/' +
                                                    $routeParams.matchId);
                                 },
                                 (errRes) => {
                                     if (errRes.status = 400 && errResp.data.error = 'effectHashMismatch') {
                                         self.effectHash = errResp.data.effectHash;
                                         self.effect = errResp.data.effectedMatches;
                                         requestStatus.complete();
                                     } else {
                                         requestStatus.failed(errRes);
                                     }
                                 });
                         };

                         self.acceptSetScore = function () {
                             $rootScope.$broadcast('event.match.set.score');
                         };

                         self.cancelSetScore = function () {
                             self.setRescoring = true;
                         };

                         self.mergeSetScore = function (setScore) {
                             setScore.scores.forEach(score => {
                                 self.match.score.sets[score.uid][setScore.setOrdNumber] = score.score;
                             });
                         };
                         self.removeLastSet = function () {
                             var keys = Object.keys(self.match.score.sets);
                             for (var i in keys) {
                                 self.match.score.sets[keys[i]].splice(-1, 1);
                             }
                         };

                         binder($scope, {
                             'event.main.menu.ready': (e) => mainMenu.setTitle('Match score editor'),
                             'event.match.set.scored': (event, setScore) => {
                                 self.setRescoring = false;
                                 self.mergeSetScore(setScore);
                             },
                             'event.base.match.set.pick.lost': (e, setScore) => {
                                 self.setRescoring = false;
                                 self.mergeSetScore(setScore);
                             },
                             'event.review.match.set.appended': (event) => {
                                 self.setRescoring = true;
                                 self.match.setScores = null;
                                 $rootScope.$broadcast('event.match.set', self.match);
                             },
                             'event.review.match.set.popped': (event) => {
                                 self.removeLastSet();
                             },
                             'event.review.match.set.picked': (event, setIdx, set) => {
                                 self.setRescoring = true;
                                 self.match.setScores = [set.a, set.b];
                                 $rootScope.$broadcast('event.match.set', self.match);
                             },
                             'event.review.match.ready': (event) => {
                                 $rootScope.$broadcast('event.review.match.config', {edit: true});
                                 requestStatus.startLoading();
                                 Match.matchResult(
                                     {tournamentId: $routeParams.tournamentId,
                                      matchId: $routeParams.matchId},
                                     function (match) {
                                         requestStatus.complete();
                                         self.match = match;
                                         $rootScope.$broadcast('event.review.match.data', match);
                                     },
                                     requestStatus.failed);
                             }
                         });
                     }]});
