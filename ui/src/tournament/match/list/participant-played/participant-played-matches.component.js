import angular from 'angular';
import template from './participant-played-matches.template.html';

angular.
    module('tournament').
    component('participantPlayedMatches', {
        templateUrl: template,
        controller: ['Match', 'mainMenu', '$routeParams', 'requestStatus', 'binder', '$scope', 'eBarier', '$rootScope',
                     function (Match, mainMenu, $routeParams, requestStatus, binder, $scope, eBarier, $rootScope) {
                         var self = this;
                         self.tournamentId = $routeParams.tournamentId;
                         var inGroupBarier = eBarier.create(['widget', 'data'], (list) => {
                             $rootScope.$broadcast('event.complete.match.list.data.inGroup', list);
                         });
                         var playOffBarier = eBarier.create(['widget', 'data'], (list) => {
                             $rootScope.$broadcast('event.complete.match.list.data.playOff', list);
                         });

                         binder($scope, {
                             'event.complete.match.list.ready.inGroup': (e) => inGroupBarier.got('widget'),
                             'event.complete.match.list.ready.playOff': (e) => playOffBarier.got('widget'),
                             'event.main.menu.ready': (e) => mainMenu.setTitle('Matches played by me'),
                             'event.request.status.ready': (event) => {
                                 requestStatus.startLoading();
                                 Match.myPlayedMatches(
                                     {tournamentId: $routeParams.tournamentId},
                                     function (matches) {
                                         requestStatus.complete();
                                         self.matches = matches;
                                         inGroupBarier.got('data', matches.inGroup);
                                         playOffBarier.got('data', matches.playOff);
                                     },
                                   (...a) => requestStatus.failed(...a));
                             }
                         });
                     }
                    ]
        });
