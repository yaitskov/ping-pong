import angular from 'angular';
import template from './tournament-rules.template.html';

angular.
    module('tournament').
    component('tournamentRules', {
        templateUrl: template,
        controller: ['Tournament', '$routeParams', 'requestStatus', 'mainMenu',
                     function (Tournament, $routeParams, requestStatus, mainMenu) {
                         mainMenu.setTitle('Tournament Rules');
                         requestStatus.startLoading();
                         var self = this;
                         Tournament.parameters(
                             {tournamentId: $routeParams.tournamentId},
                             function (rules) {
                                 requestStatus.complete();
                                 self.rules = rules;
                             },
                             requestStatus.failed);
                     }]});
