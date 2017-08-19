import angular from 'angular';
import template from './open-tournament-list.template.html';

angular.module('openTournamentList').
    component('openTournamentList', {
        templateUrl: template,
        controller: ['Tournament', 'mainMenu', 'requestStatus',
                     function (Tournament, mainMenu, requestStatus) {
                         mainMenu.setTitle('Running tournaments');
                         var self = this;
                         self.tournaments = null;
                         requestStatus.startLoading();
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
                         Tournament.running(
                             {alsoCompleteInDays: 3},
                             function (tournaments) {
                                 self.tournaments = tournaments;
                                 requestStatus.complete();
                             },
                             requestStatus.failed);
                     }
                    ]
        });
