'use strict';

angular.module('tournamentList').
    component('tournamentList', {
        templateUrl: 'tournament-list/tournament-list.template.html',
        controller: ['Tournament', 'mainMenu',
                     function (Tournament, mainMenu) {
                         mainMenu.setTitle('Drafting');
                         this.tournaments = Tournament.drafting();
                     }
                    ]
        });
