import angular from 'angular';
import template from './match-management.template.html';

angular.
    module('tournament').
    component('matchManagement', {
        templateUrl: template,
        controller: ['Match', 'mainMenu', '$routeParams', 'binder', '$rootScope', '$scope', 'requestStatus',
                     function (Match, mainMenu, $routeParams, binder, $rootScope, $scope, requestStatus) {
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
                                         self.match = match;
                                         self.isMatchStateGameOver = (match.state == 'Game' || match.state == 'Over');
                                         self.isMatchStateGameOver = (match.state == 'Game' || match.state == 'Over');
                                         self.isMatchStateDraftPlaceGame = match.state == 'Game' ||
                                             match.state == 'Draft' ||
                                             match.state == 'Place';
                                         $rootScope.$broadcast('event.review.match.data', match);
                                     },
                                     requestStatus.failed);
                              }
                         });
                         console.log('match management bound');
                     }]});
