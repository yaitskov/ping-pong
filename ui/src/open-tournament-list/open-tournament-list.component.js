'use strict';

angular.module('openTournamentList').
    component('openTournamentList', {
        templateUrl: 'open-tournament-list/open-tournament-list.template.html',
        controller: ['Tournament', 'mainMenu', 'requestStatus',
                     function (Tournament, mainMenu, requestStatus) {
                         mainMenu.setTitle('Running tournaments');
                         var self = this;
                         self.tournaments = null;
                         requestStatus.startLoading();
                         this.percent = function (tournament) {
                             if (!tournament.gamesOverall) {
                                 return '-';
                             }
                             var ratio = tournament.gamesComplete / tournament.gamesOverall;
                             return Math.round(ratio * 100.0) + '%';
                         };
                         Tournament.running(
                             {a: 3},
                             function (tournaments) {
                                 self.tournaments = tournaments;
                                 requestStatus.complete();
                             },
                             requestStatus.failed);
                     }
                    ]
        });
