import angular from 'angular';
import '../open-tournament.scss';
import '../../list.scss';
import template from './play-in-tournament-list.template.html';

angular.module('tournament').
    component('playInTournamentList', {
        templateUrl: template,
        cache: false,
        controller: ['Tournament', 'mainMenu', 'requestStatus', 'binder', '$scope',
                     function (Tournament, mainMenu, requestStatus, binder, $scope) {
                         this.tournaments = null;
                         var self = this;
                         this.viewUrl = function (tournament) {
                             return '/tournaments/' + tournament.tid;
                         }
                         binder($scope, {
                             'event.main.menu.ready': (e) => mainMenu.setTitle('Tournaments I am enlisted to'),
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
