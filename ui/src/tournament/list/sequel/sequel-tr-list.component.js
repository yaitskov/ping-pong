import angular from 'angular';
import '../../list.scss';
import template from './sequel-tr-list.template.html';

angular.module('tournament').
    component('sequelTournamentList', {
        templateUrl: template,
        controller: ['Tournament', 'mainMenu', '$routeParams', 'binder', '$scope', 'requestStatus',
                     function (Tournament, mainMenu, $routeParams, binder, $scope, requestStatus) {
                         var self = this;
                         binder($scope, {
                             'event.main.menu.ready': (e) =>
                                 mainMenu.setTitle('Following Tournaments'),
                             'event.request.status.ready': (e) => {
                                 requestStatus.startLoading();
                                 Tournament.following(
                                     {tournamentId: $routeParams.tournamentId},
                                     function (tournaments) {
                                         requestStatus.complete();
                                         self.tournaments = tournaments;
                                     },
                                   (...a) => requestStatus.failed(...a));
                             }
                         });
                     }
                    ]
        });
