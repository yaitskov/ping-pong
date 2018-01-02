import angular from 'angular';
import '../../list.scss';
import './watch-tournament-list.scss';
import '../open-tournament.scss';
import template from './watch-tournament-list.template.html';

angular.module('tournament').
    component('watchTournamentList', {
        templateUrl: template,
        controller: ['Tournament', 'mainMenu', 'requestStatus', 'binder', '$scope',
                     function (Tournament, mainMenu, requestStatus, binder, $scope) {
                         var self = this;
                         self.tournaments = null;

                         this.viewUrl = function (tournament) {
                             if (tournament.state == 'Open') {
                                 return '/watch/tournament/' + tournament.tid;
                             }
                             return '/tournament/result/' + tournament.tid;
                         }
                         this.percent = function (tournament) {
                             if (!tournament.gamesOverall) {
                                 return '-';
                             }
                             var ratio = tournament.gamesComplete / tournament.gamesOverall;
                             return Math.round(ratio * 100.0) + '%';
                         };
                         binder($scope, {
                             'event.main.menu.ready': (e) => mainMenu.setTitle('Running tournaments'),
                             'event.request.status.ready': (event) => {
                                 requestStatus.startLoading();
                                 Tournament.running(
                                     {alsoCompleteInDays: 3},
                                     function (tournaments) {
                                         self.tournaments = tournaments;
                                         requestStatus.complete();
                                     },
                                     requestStatus.failed);
                             }
                         });
                     }
                    ]
    });
