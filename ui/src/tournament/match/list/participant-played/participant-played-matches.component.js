import angular from 'angular';
import template from './participant-played-matches.template.html';

angular.
    module('tournament').
    component('participantPlayedMatches', {
        templateUrl: template,
        controller: ['Match', 'mainMenu', '$routeParams', 'requestStatus', 'binder', '$scope',
                     function (Match, mainMenu, $routeParams, requestStatus, binder, $scope) {
                         var self = this;
                         self.tournamentId = $routeParams.tournamentId;
                         binder($scope, {
                             'event.main.menu.ready': (e) => mainMenu.setTitle('Matches played by me'),
                             'event.request.status.ready': (event) => {
                                 requestStatus.startLoading();
                                 Match.myPlayedMatches(
                                     {tournamentId: $routeParams.tournamentId},
                                     function (matches) {
                                         requestStatus.complete();
                                         self.matches = matches;
                                     },
                                     requestStatus.failed);
                             }
                         });
                     }
                    ]
        });
