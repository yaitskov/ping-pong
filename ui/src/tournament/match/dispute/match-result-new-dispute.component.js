import angular from 'angular';
import template from './match-result-new-dispute.template.html';

angular.
    module('tournament').
    component('matchResultNewDispute', {
        templateUrl: template,
        controller: ['MatchDispute', 'Match', 'mainMenu', '$routeParams',
                     'binder', '$rootScope', '$scope', 'requestStatus', '$location',
                     function (MatchDispute, Match, mainMenu, $routeParams,
                               binder, $rootScope, $scope, requestStatus, $location) {
                         mainMenu.setTitle('Dispute builder');
                         var self = this;
                         self.tournamentId = $routeParams.tournamentId;
                         self.matchId = $routeParams.matchId;
                         self.setRescoring = false;
                         self.match = null;
                         self.openDispute = function () {
                             requestStatus.startLoading();
                             MatchDispute.openDispute(
                                 {tid: self.match.tid,
                                  mid: self.match.score.mid,
                                  sets: self.match.score.sets},
                                 function (ok) {
                                     requestStatus.complete();
                                     $location.path('/tournament/my-complete-match/' +
                                                    $routeParams.tournamentId + '/' +
                                                    $routeParams.matchId);
                                 },
                                 requestStatus.failed);
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
                             'event.match.set.scored': (event, setScore) => {
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
                             'event.match.review.ready': (event) => {
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
