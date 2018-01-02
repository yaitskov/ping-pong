import angular from 'angular';
import '../../list.scss';
import template from './manage-tr-list.template.html';

angular.module('tournament').
    component('manageTournamentList', {
        templateUrl: template,
        controller: ['Tournament', 'mainMenu', '$rootScope', 'binder', '$scope', 'requestStatus',
                     function (Tournament, mainMenu, $rootScope, binder, $scope, requestStatus) {
                         var self = this;
                         binder($scope, {
                             'event.main.menu.ready': (e) =>
                                 mainMenu.setTitle('AdministratedTournaments',
                                                   {'#!/tournament/new': 'AddTournament'}),
                             'event.request.status.ready': (e) => {
                                 requestStatus.startLoading();
                                 Tournament.administered(
                                     {completeInDays: 30},
                                     function (tournaments) {
                                         requestStatus.complete();
                                         self.tournaments = tournaments;
                                     },
                                     requestStatus.failed);
                             }
                         });
                     }
                    ]
        });
