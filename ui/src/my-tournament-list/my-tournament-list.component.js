import angular from 'angular';
import template from './my-tournament-list.template.html';

angular.module('myTournamentList').
    component('myTournamentList', {
        templateUrl: template,
        cache: false,
        controller: ['Tournament', 'mainMenu', '$translate',
                     function (Tournament, mainMenu, $translate) {
                         console.log("init my tournament list");
                         $translate(['AddTournament', 'AdministratedTournaments']).then(function (translations) {
                             mainMenu.setTitle(translations.AdministratedTournaments);
                             mainMenu.setContextMenu({'#!/tournament/new': translations.AddTournament});
                         });
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
