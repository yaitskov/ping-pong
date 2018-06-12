import angular from 'angular';
import template from './admin-score-set.template.html';

angular.
    module('tournament').
    component('adminScoreSet', {
        templateUrl: template,
        controller: ['mainMenu', '$routeParams', 'pageCtx', '$scope', '$location',
                     '$rootScope', 'binder', 'eBarier', 'Match', 'requestStatus',
                     function (mainMenu, $routeParams, pageCtx, $scope, $location,
                               $rootScope, binder, eBarier, Match, requestStatus) {
                         var self = this;
                         var matchReady = eBarier.create(
                             ['score.widget.ready', 'match.loaded'],
                             (match) => $rootScope.$broadcast('event.match.set', self.match = match));
                         self.showConflict = function (conflict) {
                             pageCtx.put('match-score-conflict-' + $routeParams.matchId, conflict);
                             $location.path('/match/admin-conflict-review/' + self.match.tid + '/' + $routeParams.matchId);
                         };
                         binder($scope, {
                             'event.request.status.ready': (e) => {
                                 self.match = pageCtx.get('last-scoring-match');
                                 if (!self.match || self.match.mid != $routeParams.matchId) {
                                     self.match = null;
                                     requestStatus.startLoading();
                                     Match.matchForJudge(
                                         {tournamentId: $routeParams.tournamentId,
                                          matchId: $routeParams.matchId},
                                         function (match) {
                                             requestStatus.complete();
                                             matchReady.got('match.loaded', match);
                                         },
                                       (...a) => requestStatus.failed(...a));
                                 } else {
                                     matchReady.got('match.loaded', self.match);
                                 }
                             },
                             'event.main.menu.ready': (e) => mainMenu.setTitle('Match Scoring'),
                             'event.match.set.ready': (event) => matchReady.got('score.widget.ready'),
                             'event.match.score.conflict': (event, conflict) => self.showConflict(conflict),
                             'event.match.scored': (event, matchScore) => {
                                 pageCtx.put('match-score-review-' + $routeParams.matchId,
                                             {score: matchScore,
                                              participants: self.match.participants
                                             });
                                 $location.path('/review/admin-scored-match/' + self.match.tid + '/' + $routeParams.matchId);
                             }
                         });
                     }]});
