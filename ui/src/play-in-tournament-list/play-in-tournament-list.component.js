'use strict';

angular.module('playInTournamentList').
    component('playInTournamentList', {
        templateUrl: 'play-in-tournament-list/play-in-tournament-list.template.html',
        cache: false,
        controller: ['Tournament', 'mainMenu', 'requestStatus',
                     function (Tournament, mainMenu, requestStatus) {
                         mainMenu.setTitle('Coming tournaments where I am enlisted');
                         this.tournaments = null;
                         var self = this;
                         requestStatus.startLoading();
                         Tournament.participateIn(
                             {},
                             function (tournaments) {
                                 requestStatus.complete();
                                 self.tournaments = tournaments;
                             },
                             requestStatus.failed);
                     }
                    ]
    });
