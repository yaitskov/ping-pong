import angular from 'angular';
import template from './play-in-tournament-list.template.html';

angular.module('tournament').
    component('playInTournamentList', {
        templateUrl: template,
        cache: false,
        controller: ['Tournament', 'mainMenu', 'requestStatus', 'binder', '$scope',
                     function (Tournament, mainMenu, requestStatus, binder, $scope) {
                         mainMenu.setTitle('Tournaments I am enlisted to');
                         this.tournaments = null;
                         var self = this;
                         this.viewUrl = function (tournament) {
                             return '/tournaments/' + tournament.tid;
                         }
                         binder($scope, {
                             'event.request.status.ready': (event) => {
                                 requestStatus.startLoading();
                                 Tournament.participateIn(
                                     {completeAfterDays: 3},
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
