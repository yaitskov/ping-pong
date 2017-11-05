import angular from 'angular';
import template from './tournament-rules.template.html';

angular.
    module('tournament').
    component('tournamentRules', {
        templateUrl: template,
        controller: ['Tournament', '$routeParams', 'requestStatus', 'mainMenu', 'binder', '$scope',
                     function (Tournament, $routeParams, requestStatus, mainMenu, binder, $scope) {
                         mainMenu.setTitle('Tournament Rules');

                         var self = this;
                         binder($scope, {
                             'event.request.status.ready': (event) => {
                                 requestStatus.startLoading();
                                 Tournament.parameters(
                                     {tournamentId: $routeParams.tournamentId},
                                     function (rules) {
                                         requestStatus.complete();
                                         self.rules = rules;
                                     },
                                     requestStatus.failed);
                             }
                         });
                     }]});
