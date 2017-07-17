'use strict';

angular.module('tournamentList').
    component('tournamentList', {
        templateUrl: 'tournament-list/tournament-list.template.html',
        controller: ['Tournament', 'mainMenu', '$location',
                     function (Tournament, mainMenu, $location) {
                         mainMenu.setTitle('Drafting');
                         this.tournaments = null;
                         var self = this;
                         self.error = null;
                         this.goTo = function (tid) {
                             $location.path('/tournaments/' + tid);
                         };
                         Tournament.drafting(
                             {},
                             function (tournaments) {
                                 self.error = 0;
                                 self.tournaments = tournaments;
                             },
                             function (bad) {
                                 self.error = "Failed " + bad.status;
                             });

                     }
                    ]
        });
