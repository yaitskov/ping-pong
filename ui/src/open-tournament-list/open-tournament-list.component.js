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
                         Tournament.running(
                             {},
                             function (tournaments) {
                                 self.tournaments = tournaments;
                                 requestStatus.complete();
                             },
                             requestStatus.failed);
                     }
                    ]
        });
