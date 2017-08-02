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
                         this.viewUrl = function (tournament) {
                             if (tournament.state == 'Close') {
                                 return '/tournament/result/' + tournament.tid;
                             }
                             return '/tournaments/' + tournament.tid;
                         }
                         Tournament.participateIn(
                             {completeAfterDays: 3},
                             function (tournaments) {
                                 requestStatus.complete();
                                 self.tournaments = tournaments;
                             },
                             requestStatus.failed);
                     }
                    ]
    });
