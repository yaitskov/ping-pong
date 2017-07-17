'use strict';

angular.module('tournamentList').
    component('tournamentList', {
        templateUrl: 'tournament-list/tournament-list.template.html',
        controller: ['Tournament', 'mainMenu', '$location', 'requestStatus',
                     function (Tournament, mainMenu, $location, requestStatus) {
                         mainMenu.setTitle('Drafting');
                         requestStatus.setup('Tournament');
                         this.tournaments = null;
                         var self = this;
                         requestStatus.startLoading();
                         this.goTo = function (tid) {
                             $location.path('/tournaments/' + tid);
                         };
                         Tournament.drafting(
                             {},
                             function (tournaments) {
                                 self.tournaments = tournaments;
                                 requestStatus.complete(tournaments);
                             },
                             requestStatus.failed);
                     }
                    ]
        });
