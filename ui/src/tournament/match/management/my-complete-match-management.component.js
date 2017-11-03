import angular from 'angular';
import template from './my-complete-match-management.template.html';

angular.
    module('tournament').
    component('myCompleteMatchManagement', {
        templateUrl: template,
        controller: ['mainMenu', '$routeParams', 'binder', '$rootScope', '$scope',
                     function (mainMenu, $routeParams, binder, $rootScope, $scope) {
                         mainMenu.setTitle('Match management');
                         var self = this;
                         self.tournamentId = $routeParams.tournamentId;
                         self.matchId = $routeParams.matchId;

                     }]});
