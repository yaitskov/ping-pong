import angular from 'angular';
import template from './match-management.template.html';

angular.
    module('tournament').
    component('matchManagement', {
        templateUrl: template,
        controller: ['Match', 'mainMenu', '$routeParams', 'binder', '$rootScope', '$scope', 'requestStatus', 'pageCtx',
                     function (Match, mainMenu, $routeParams, binder, $rootScope, $scope, requestStatus, pageCtx) {
                         var self = this;
                         self.tournamentId = $routeParams.tournamentId;
                         self.matchId = $routeParams.matchId;
                         binder($scope, {
                             'event.main.menu.ready': (e) => {
                                 mainMenu.setTitle('Match management');
                             },
                             'event.review.match.ready': (event) => {
                                 requestStatus.startLoading();
                                 Match.matchResult(
                                     {tournamentId: $routeParams.tournamentId,
                                      matchId: $routeParams.matchId},
                                     function (match) {
                                         requestStatus.complete();
                                         pageCtx.put('last-scoring-match', match);
                                         self.match = match;
                                         self.isMatchStateGameOver = (match.state == 'Game' || match.state == 'Over');
                                         self.isMatchStateDraftPlaceGame = match.state == 'Game' ||
                                             match.state == 'Draft' ||
                                             match.state == 'Place';
                                         $rootScope.$broadcast('event.review.match.data', match);
                                     },
                                   (...a) => requestStatus.failed(...a));
                              }
                         });
                         console.log('match management bound');
                     }]});
