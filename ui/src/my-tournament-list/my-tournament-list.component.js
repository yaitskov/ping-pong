import angular from 'angular';
import template from './my-tournament-list.template.html';

angular.module('myTournamentList').
    component('myTournamentList', {
        templateUrl: template,
        cache: false,
        controller: ['Tournament', 'mainMenu',
                     function (Tournament, mainMenu) {
                         mainMenu.setTitle('AdministratedTournaments', {'#!/tournament/new': 'AddTournament'});
                         this.tournaments = null;
                         var self = this;
                         Tournament.administered(
                             {completeInDays: 30},
                             function (tournaments) {
                                 self.tournaments = tournaments;
                             });
                     }
                    ]
        });
