import angular from 'angular';
import 'css/toggle-btn.scss';
import template from './tournament-rules.template.html';

angular.
    module('tournament').
    component('tournamentRules', {
        templateUrl: template,
        controller: ['Tournament', '$routeParams', 'requestStatus', 'mainMenu', 'binder', '$scope',
                     function (Tournament, $routeParams, requestStatus, mainMenu, binder, $scope) {
                         var self = this;
                         binder($scope, {
                             'event.main.menu.ready': (e) => mainMenu.setTitle('Tournament Rules'),
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
