import angular from 'angular';
import template from './my-complete-match-management.template.html';

angular.
    module('tournament').
    component('myCompleteMatchManagement', {
        templateUrl: template,
        controller: ['Match', 'mainMenu', '$routeParams', 'binder', '$rootScope', '$scope', 'requestStatus',
                     function (Match, mainMenu, $routeParams, binder, $rootScope, $scope, requestStatus) {
                         var self = this;
                         self.tournamentId = $routeParams.tournamentId;
                         self.matchId = $routeParams.matchId;
                         binder($scope, {
                             'event.main.menu.ready': (e) => mainMenu.setTitle('Match management'),
                             'event.match.review.ready': (event) => {
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
