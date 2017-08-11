'use strict';

angular.module('myTournamentList').
    component('myTournamentList', {
        templateUrl: 'my-tournament-list/my-tournament-list.template.html',
        cache: false,
        controller: ['Tournament', 'mainMenu',
                     function (Tournament, mainMenu) {
                         console.log("init my tournament list");
                         mainMenu.setTitle('Administrated tournaments');
                         mainMenu.setContextMenu({'#!/tournament/new': 'Add Tournament'});
                         this.tournaments = null;
                         var self = this;
                         Tournament.administered(
                             {completeInDays: 30},
                             function (tournaments) {
                                 console.log("Loaded tournaments " + tournaments.length);
                                 self.tournaments = tournaments;
                             });
                     }
                    ]
        });
